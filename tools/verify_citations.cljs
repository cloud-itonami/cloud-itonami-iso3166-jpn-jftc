#!/usr/bin/env nbb
;; Live citation gate for src/statute/facts.cljc.
;;
;; Re-fetches the official e-Gov Laws API v2 and asserts TWO things:
;;
;;   1. PRESENCE -- for every entry in `catalog`, the API still returns the
;;      byte-exact `law_title` recorded in :statute/verified-label, the
;;      `law_num` in :statute/verified-law-num, and the `promulgation_date`
;;      in :statute/promulgation-date, for that :statute/egov-law-id.
;;   2. ABSENCE  -- every :absence/absent-title in `absences` still matches
;;      zero laws (`total_count` 0) when searched as a title.
;;
;; Any drift, any missing law, any absence that stopped being true, any
;; unreachable API => non-zero.
;;
;;   nbb tools/verify_citations.cljs
;;
;; Why check absences at all. A gate that only re-checks what we wrote down
;; can only ever tell us our positives rotted. It cannot tell us the world
;; grew something we recorded as missing. `jpn-jftc.no-establishment-act`
;; says no 公正取引委員会設置法 exists; if the Diet ever enacts one, a
;; presence-only gate stays green while the catalog's central claim quietly
;; goes false.
;;
;; Why titles are worth re-fetching: the Subcontract Act was renamed
;; effective 2026-01-01 (令和七年法律第四十一号). A memory-written catalog
;; would still say 下請代金支払遅延等防止法. This gate is what catches that.
;;
;; Why this does NOT curl www.jftc.go.jp -- the agency site answers automated
;; clients with HTTP 403, so nothing in the catalog cites it. We verify
;; through the documented machine API (laws.e-gov.go.jp/api/2) instead.
;;
;; Exit codes are three-valued on purpose: 0 verified, 1 drifted/mismatched,
;; 2 could-not-answer (network/API down). 2 must never be read as a pass.
(ns verify-citations
  (:require [clojure.string :as str]))

(def catalog-file "src/statute/facts.cljc")

(defn- die [code & msg]
  (println (str/join " " msg))
  (js/process.exit code))

;; ── read the catalog without needing a Clojure runtime ────────────────────
;; We parse the entries out of the .cljc source so this gate has no build
;; step. Each entry is a map literal; we pull the fields we check.
(defn- source []
  (.readFileSync (js/require "fs") catalog-file "utf8"))

(defn- entries [src]
  (let [blocks (rest (str/split src #"\{:statute/id "))]
    (mapv (fn [b]
            (let [f (fn [re] (second (re-find re b)))]
              {:id (f #"^\"([^\"]+)\"")
               :law-id (f #":statute/egov-law-id \"([^\"]+)\"")
               :label (f #":statute/verified-label \"((?:[^\"\\]|\\.)*)\"")
               :law-num (f #":statute/verified-law-num \"([^\"]+)\"")
               :promulgated (f #":statute/promulgation-date \"([^\"]+)\"")
               :api (f #":statute/verified-via \"([^\"]+)\"")}))
          blocks)))

(defn- absence-entries [src]
  (let [blocks (rest (str/split src #"\{:absence/id "))]
    (mapv (fn [b]
            (let [f (fn [re] (second (re-find re b)))]
              {:id (f #"^\"([^\"]+)\"")
               :absent-title (f #":absence/absent-title \"([^\"]+)\"")
               :api (f #":absence/verified-via \"([^\"]+)\"")}))
          blocks)))

(defn- fetch-json [url]
  (-> (js/fetch url)
      (.then (fn [r]
               (when-not (.-ok r)
                 (die 2 "CANNOT-ANSWER: e-Gov API returned HTTP" (.-status r) "for" url))
               (.json r)))
      (.catch (fn [e] (die 2 "CANNOT-ANSWER: e-Gov API unreachable:" (str e))))))

(defn- first-law
  "laws[0] of an API v2 search response, or nil."
  [doc]
  (first (.-laws doc)))

(defn- check-presence [e doc]
  (let [law (first-law doc)]
    (if (nil? law)
      [:missing (:id e) (str "law_id " (:law-id e) " returned no laws from " (:api e))]
      (let [info (.-law_info law)
            title (.-law_title (.-revision_info law))
            num (.-law_num info)
            prom (.-promulgation_date info)]
        (cond
          (not= (:label e) title)
          [:drift (:id e) (str "recorded law_title " (pr-str (:label e))
                               " but API says " (pr-str title))]
          (not= (:law-num e) num)
          [:drift (:id e) (str "recorded law_num " (pr-str (:law-num e))
                               " but API says " (pr-str num))]
          (not= (:promulgated e) prom)
          [:drift (:id e) (str "recorded promulgation_date " (pr-str (:promulgated e))
                               " but API says " (pr-str prom))]
          :else [:ok (:id e) title])))))

(defn- check-absence [a doc]
  (let [n (.-total_count doc)]
    (if (zero? n)
      [:ok (:id a) (str "still absent: title search for " (pr-str (:absent-title a))
                        " matches 0 laws")]
      [:appeared (:id a)
       (str "recorded as ABSENT, but a title search for " (pr-str (:absent-title a))
            " now matches " n " law(s) — the catalog's claim has gone false")])))

(defn -main []
  (let [src (source)
        es (entries src)
        as (absence-entries src)]
    (when (empty? es)
      (die 2 "CANNOT-ANSWER: parsed 0 entries from" catalog-file
           "— the gate could not read what it is supposed to check"))
    (when (empty? as)
      (die 2 "CANNOT-ANSWER: parsed 0 absences from" catalog-file
           "— the absence half of this gate had nothing to check"))
    (when-let [broken (first (remove #(and (:law-id %) (:label %) (:law-num %)
                                           (:promulgated %) (:api %))
                                     es))]
      (die 2 "CANNOT-ANSWER: entry" (pr-str (:id broken))
           "is missing one of the verified fields — refusing to skip it"))
    (println (str "SCANNED\t" (count es) " citations and " (count as)
                  " absences from " catalog-file))
    (-> (js/Promise.all (clj->js (map fetch-json (concat (map :api es) (map :api as)))))
        (.then
         (fn [docs]
           (let [docs (vec docs)
                 results (concat
                          (map-indexed (fn [i e] (check-presence e (nth docs i))) es)
                          (map-indexed (fn [i a] (check-absence a (nth docs (+ (count es) i)))) as))
                 bad (remove #(= :ok (first %)) results)]
             (doseq [[status id detail] results]
               (println (if (= :ok status) "  OK  " "  FAIL") id "—" detail))
             (println (str "VERIFIED\t" (count (filter #(= :ok (first %)) results))
                           " / " (count results)))
             (if (seq bad)
               (die 1 "FAIL:" (count bad) "claim(s) do not match the official e-Gov Laws API")
               (println "PASS: every citation and every recorded absence matches the official e-Gov Laws API"))))))))

(-main)

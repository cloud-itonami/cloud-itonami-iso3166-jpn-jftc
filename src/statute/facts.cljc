(ns statute.facts
  "Agency-level compliance catalog for **JPN-JFTC** (公正取引委員会, Japan Fair
  Trade Commission) -- the spec-basis behind this leaf's blueprint claim that an
  independent operator can navigate `JFTC antitrust & bid-rigging compliance`.

  Scope. This is the JFTC-specific layer only: the statutes the JFTC itself
  administers and the cabinet order organising its secretariat. Government-wide
  Japanese statutes (会社法, 労働基準法, ...) are NOT duplicated here; catalogs
  compose keyed `JPN-JFTC` -> `JPN`, matching `blueprint.edn`'s
  `:itonami.blueprint/iso3166` / `:itonami.blueprint/iso3166-parent`.

  Provenance. Every entry cites the official e-Gov 法令検索 (Laws of Japan,
  Digital Agency) address for the law that was independently confirmed.
  Nothing here is fabricated: each `:statute/verified-label` below is the
  byte-exact `law_title` returned by the e-Gov Laws API v2 on
  `:statute/verified-at`, together with the `law_num` and `promulgation_date`
  from the same response, and `tools/verify_citations.cljs` re-fetches that
  API and fails if any of the three drifts.

  Why titles are re-fetched instead of trusted. Statute titles in this domain
  actually move: the Subcontract Act (下請代金支払遅延等防止法, entry
  `jpn-jftc.subcontract-act`) was RENAMED by 令和七年法律第四十一号 effective
  2026-01-01 to 製造委託等に係る中小受託事業者に対する代金の支払の遅延等の
  防止に関する法律. A catalog written from memory in 2025 would cite a title
  that is no longer the law's name. The live gate exists to catch exactly this.

  Note on www.jftc.go.jp. The agency's own site answers automated clients
  with HTTP 403, so nothing here cites it as a verified source -- a URL this
  repo cannot re-fetch cannot sit in a verified catalog. The official site is
  recorded (unverified, human-only) in `organization.edn`.

  What this catalog also records is an ABSENCE. See `absences` below: there
  is no standalone 公正取引委員会設置法. Unlike most Japanese agencies, which
  are created by their own establishment act (総務省設置法, ...), the JFTC is
  established directly by the Antimonopoly Act itself (chapter VIII,
  article 27). An operator searching e-Gov for a JFTC establishment act finds
  nothing, and would otherwise conclude this catalog is missing one. That is
  a checked fact -- the live gate re-runs the title search and fails if a law
  with that title ever appears.

  Extending. A statute not in this table has NO spec-basis, full stop. Extend
  `catalog` with a real, API-confirmed citation; never invent an id, a URL,
  a law number, or a title."
  (:require [clojure.string :as str]))

(def egov-laws-api
  "The e-Gov Laws API v2 search endpoint these entries were verified against.
  Append `law_id=<:statute/egov-law-id>` for presence checks; the absence
  check queries `law_title=<title>` and expects `total_count` 0."
  "https://laws.e-gov.go.jp/api/2/laws")

(def catalog
  "iso3166 code -> vector of statute entries.

  `JPN-JFTC` is an agency-level key (parent `JPN`), matching
  `blueprint.edn`'s `:itonami.blueprint/iso3166`."
  {"JPN-JFTC"
   [;; ── 独占禁止法 — the act that both empowers and *establishes* the JFTC ──
    {:statute/id "jpn-jftc.antimonopoly-act"
     :statute/title "私的独占の禁止及び公正取引の確保に関する法律（独占禁止法） — core antitrust act; also establishes the JFTC (ch. VIII, art. 27)"
     :statute/jurisdiction "JPN-JFTC"
     :statute/kind :act
     :statute/law-number "昭和二十二年法律第五十四号"
     :statute/url "https://laws.e-gov.go.jp/law/322AC0000000054"
     :statute/url-provenance :official-egov
     :statute/egov-law-id "322AC0000000054"
     :statute/verified-via "https://laws.e-gov.go.jp/api/2/laws?law_id=322AC0000000054"
     :statute/verified-label "昭和二十二年法律第五十四号（私的独占の禁止及び公正取引の確保に関する法律）"
     :statute/verified-law-num "昭和二十二年法律第五十四号"
     :statute/promulgation-date "1947-04-14"
     :statute/verified-at "2026-08-26"
     :statute/topic #{:antimonopoly :cartel :agency-establishment}}

    ;; ── 取適法（旧・下請法）— renamed effective 2026-01-01 ─────────────────
    ;; The former 下請代金支払遅延等防止法. 令和七年法律第四十一号 renamed it
    ;; and the new title below is what the API returns today. Operator-facing
    ;; JFTC material may still say 下請法; the law number is the stable handle.
    {:statute/id "jpn-jftc.subcontract-act"
     :statute/title "中小受託取引適正化法（取適法・旧 下請法） — subcontractor payment fairness"
     :statute/jurisdiction "JPN-JFTC"
     :statute/kind :act
     :statute/law-number "昭和三十一年法律第百二十号"
     :statute/url "https://laws.e-gov.go.jp/law/331AC0000000120"
     :statute/url-provenance :official-egov
     :statute/egov-law-id "331AC0000000120"
     :statute/verified-via "https://laws.e-gov.go.jp/api/2/laws?law_id=331AC0000000120"
     :statute/verified-label "製造委託等に係る中小受託事業者に対する代金の支払の遅延等の防止に関する法律"
     :statute/verified-law-num "昭和三十一年法律第百二十号"
     :statute/promulgation-date "1956-06-01"
     :statute/verified-at "2026-08-26"
     :statute/topic #{:subcontractor-fairness :payment-terms}}

    ;; ── 官製談合防止法 — the bid-rigging half of this blueprint ────────────
    {:statute/id "jpn-jftc.bid-rigging-involvement-act"
     :statute/title "入札談合等関与行為防止法（官製談合防止法） — bid-rigging facilitation by public officials"
     :statute/jurisdiction "JPN-JFTC"
     :statute/kind :act
     :statute/law-number "平成十四年法律第百一号"
     :statute/url "https://laws.e-gov.go.jp/law/414AC1000000101"
     :statute/url-provenance :official-egov
     :statute/egov-law-id "414AC1000000101"
     :statute/verified-via "https://laws.e-gov.go.jp/api/2/laws?law_id=414AC1000000101"
     :statute/verified-label "入札談合等関与行為の排除及び防止並びに職員による入札等の公正を害すべき行為の処罰に関する法律"
     :statute/verified-law-num "平成十四年法律第百一号"
     :statute/promulgation-date "2002-07-31"
     :statute/verified-at "2026-08-26"
     :statute/topic #{:bid-rigging-prevention :public-procurement}}

    ;; ── フリーランス法 — newest JFTC-administered trade-fairness act ───────
    {:statute/id "jpn-jftc.freelance-act"
     :statute/title "フリーランス・事業者間取引適正化等法 — freelance contractor trade fairness"
     :statute/jurisdiction "JPN-JFTC"
     :statute/kind :act
     :statute/law-number "令和五年法律第二十五号"
     :statute/url "https://laws.e-gov.go.jp/law/505AC0000000025"
     :statute/url-provenance :official-egov
     :statute/egov-law-id "505AC0000000025"
     :statute/verified-via "https://laws.e-gov.go.jp/api/2/laws?law_id=505AC0000000025"
     :statute/verified-label "特定受託事業者に係る取引の適正化等に関する法律"
     :statute/verified-law-num "令和五年法律第二十五号"
     :statute/promulgation-date "2023-05-12"
     :statute/verified-at "2026-08-26"
     :statute/topic #{:subcontractor-fairness :freelance}}

    ;; ── 事務総局組織令 — how the agency itself is organised ────────────────
    {:statute/id "jpn-jftc.secretariat-organization-order"
     :statute/title "公正取引委員会事務総局組織令 — cabinet order organising the JFTC General Secretariat"
     :statute/jurisdiction "JPN-JFTC"
     :statute/kind :cabinet-order
     :statute/law-number "昭和二十七年政令第三百七十三号"
     :statute/url "https://laws.e-gov.go.jp/law/327CO0000000373"
     :statute/url-provenance :official-egov
     :statute/egov-law-id "327CO0000000373"
     :statute/verified-via "https://laws.e-gov.go.jp/api/2/laws?law_id=327CO0000000373"
     :statute/verified-label "公正取引委員会事務総局組織令"
     :statute/verified-law-num "昭和二十七年政令第三百七十三号"
     :statute/promulgation-date "1952-08-30"
     :statute/verified-at "2026-08-26"
     :statute/topic #{:agency-organization}}]})

(def absences
  "Checked absences: things an operator would reasonably look for and must
  not find. Each is re-verified by the live gate, because an absence recorded
  once and never re-checked quietly goes false the day the world changes."
  [{:absence/id "jpn-jftc.no-establishment-act"
    :absence/absent-title "公正取引委員会設置法"
    :absence/verified-via "https://laws.e-gov.go.jp/api/2/laws?law_title=%E5%85%AC%E6%AD%A3%E5%8F%96%E5%BC%95%E5%A7%94%E5%93%A1%E4%BC%9A%E8%A8%AD%E7%BD%AE%E6%B3%95"
    :absence/expect :total-count-zero
    :absence/note "Unlike ministries created by a 設置法, the JFTC is established directly by the Antimonopoly Act (昭和二十二年法律第五十四号) chapter VIII, article 27. No law whose title contains 公正取引委員会設置法 exists on e-Gov."
    :absence/verified-at "2026-08-26"}])

(defn entries
  "All statute entries for a jurisdiction key (default JPN-JFTC)."
  ([] (entries "JPN-JFTC"))
  ([iso3166] (get catalog iso3166 [])))

(defn citations
  "Every verified citation in the catalog, flat."
  []
  (into [] (mapcat val) catalog))

(defn by-topic
  "Entries whose :statute/topic contains `topic`."
  [topic]
  (filterv #(contains? (:statute/topic %) topic) (citations)))

(defn coverage
  "Honest coverage report: what was asked for vs what has a spec-basis.
  Never reports a jurisdiction as covered because it looks plausible."
  ([] (coverage (keys catalog)))
  ([iso3166s]
   (let [have (filter catalog iso3166s)
         missing (remove catalog iso3166s)]
     {:requested (count iso3166s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :citation-count (count (citations))
      :absence-count (count absences)
      :note (str "cloud-itonami-iso3166-jpn-jftc statute.facts: "
                 (count (get catalog "JPN-JFTC"))
                 " JFTC-administered statutes/orders plus "
                 (count absences)
                 " checked absence(s), each confirmed against the official "
                 "e-Gov Laws API v2 on 2026-08-26. Government-wide Japanese "
                 "statutes are NOT here -- they belong to the JPN country "
                 "layer. Extend `statute.facts/catalog`; never invent an id, "
                 "a URL, a law number, or a title.")})))

(defn official-url?
  "True when `u` is an e-Gov law address. The only provenance this catalog
  accepts for `:statute/url`."
  [u]
  (and (string? u) (str/starts-with? u "https://laws.e-gov.go.jp/law/")))

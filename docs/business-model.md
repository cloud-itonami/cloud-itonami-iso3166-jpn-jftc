# Business Model: Independent JFTC Antitrust & Bid-Rigging Compliance Service — Japan (JFTC)

## Classification

- Repository: `cloud-itonami-iso3166-jpn-jftc`
- ISO 3166 (agency-level): `JPN-JFTC`, parent `JPN`
- Ooyake cross-reference: `gov.jpn.competition` (Fair Trade Commission / 公正取引委員会)
- Activity: bid-rigging (談合) prevention under the Antimonopoly Act (私的独占の禁止及び公正取引の確保に関する法律/独占禁止法), and prime/subcontractor fair-dealing compliance under the Subcontract Act (下請代金支払遅延等防止法/下請法)
- Social impact: [:bid-rigging-prevention :subcontractor-fairness :public-spend-transparency]

## Customer

- an operator bidding on a large public tender who needs a documented bid-rigging prevention compliance program
- a prime contractor managing subcontractor relationships under 下請法 on a public-infrastructure contract
- an operator responding to a JFTC compliance inquiry tied to a public tender

## Offer

- bid-rigging-risk self-screening checklist for public tender participation
- Subcontract Act (下請法) compliance checklist for prime/subcontractor relationships
- documented antitrust compliance program suitable for tender-qualification submission
- compliance-audit export package for the operator's own records

## Revenue

- per-engagement compliance-review fee
- recurring regulatory-change monitoring subscription
- compliance-audit export package

## Trust Controls

- any actual filing, registration, or compliance-program submission
  requires Antitrust Compliance Governor clearance and always escalates to human
  sign-off (`:filing/submit` is never automated at any phase)
- a false or fabricated regulatory-requirement claim is a HARD hold that
  cannot be overridden by human approval alone — it must be corrected
  against a cited JFTC source first
- this service does **not** provide legal or tax advice; characterization
  and filing on the client's behalf beyond checklist/draft assistance
  routes to Japan-licensed counsel or a registered agent
- every requirement cites the official JFTC source or
  regulation, never invented

## Boundary with adjacent actors (read before forking)

- **`cloud-itonami-iso3166-jpn`**: the COUNTRY-level coordinator (general
  Japan public-sector market entry). This repo is a narrower, deeper
  AGENCY-level leaf — most operators need the country-level blueprint plus
  only the agency-level blueprints that actually apply to their contract.
- **`com-etzhayyim-ooyake`** (etzhayyim/root): read-only civic-wayfinding
  mirror of government structure, non-commercial, barred from acting as or
  for the government (G3 impersonation ban). This blueprint is commercial
  and never claims to be Fair Trade Commission or an official channel.
- **`matsurigoto`** (etzhayyim/root): sovereign e-government statecraft —
  literally the government. This blueprint is an independent operator that
  engages with JFTC under its public rules — never the
  agency itself.
- **`com-etzhayyim-toritsugi`** (etzhayyim/root): guides a consenting
  INDIVIDUAL citizen through their OWN procedure, non-profit,
  donation-only. This blueprint's client is a business operator, not an
  individual citizen, and it is commercial.
- **`cloud-itonami-M6910`**: helps a client BECOME a legal entity
  (incorporation, ISIC 6910) — a prior, different regulatory phase (company
  law). This blueprint assumes incorporation is already done and handles
  JFTC-specific compliance (a different regulatory domain).

#!/usr/bin/env python3
"""
Smart HTML vs JSON comparison for CommercialCibil report.
Extracts individual atomic values from HTML and checks if each 
exists in the JSON structure. Reports mismatches by section.
"""

import json, re, warnings
from bs4 import BeautifulSoup, XMLParsedAsHTMLWarning

warnings.filterwarnings("ignore", category=XMLParsedAsHTMLWarning)

HTML_FILE = "/Users/adarshgani/Downloads/CommercialCibilHpHtml-202510091760006688944-228919014.html"
JSON_FILE = "/Users/adarshgani/Downloads/CommercialCibilHpParsedResp-202510091760006688945-3861328991.json"

with open(HTML_FILE, encoding="utf-8") as f:
    soup = BeautifulSoup(f, "lxml")
with open(JSON_FILE) as f:
    jdata = json.load(f)

# ── Flatten JSON to a set of all individual values ────────────────────────────
def flatten(obj, prefix=""):
    items = {}
    if isinstance(obj, dict):
        for k, v in obj.items():
            items.update(flatten(v, f"{prefix}.{k}" if prefix else k))
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            items.update(flatten(v, f"{prefix}[{i}]"))
    else:
        items[prefix] = str(obj).strip() if obj is not None else ""
    return items

flat_json = flatten(jdata)

def norm(s):
    """Normalize a string for comparison: strip whitespace, collapse spaces."""
    s = str(s).strip()
    s = re.sub(r'\s+', ' ', s)
    return s

json_values_set = set(norm(v).lower() for v in flat_json.values() if norm(v))

def in_json(val):
    """Check if a value (or its numeric-normalized form) is in JSON."""
    v = norm(val).lower()
    if not v or v in ("-", "na", "n/a", "--", "none", ""):
        return True  # blanks are okay
    if v in json_values_set:
        return True
    # strip currency/commas
    stripped = re.sub(r"[₹,\s%]", "", v)
    if stripped and stripped in json_values_set:
        return True
    return False

# ── Extract specific sections from HTML for focused comparison ─────────────────

def text(el):
    return re.sub(r'\s+', ' ', el.get_text(separator=" ", strip=True)) if el else ""

# Section 1: Header Info
print("=" * 70)
print("SECTION 1: HEADER / BASIC INFO")
print("=" * 70)
cc = jdata.get("commCreditData", {})
h = cc.get("tuefHeader", {})
es = cc.get("executiveSummary", {})
bp = es.get("borrowerProfileSec", {}).get("borrowerDetails", {})

header_checks = {
    "Report Order Number": h.get("reportOrderNumber", ""),
    "Date Processed": h.get("dateProcessed", ""),
    "Member": h.get("memberDetails", ""),
    "Application Ref No": h.get("applicationReferenceNumber", ""),
    "Enquiry Purpose": h.get("enquiryPurpose", ""),
    "Company Name": bp.get("name", ""),
    "Legal Constitution": bp.get("legalConstitution", ""),
    "Business Category": bp.get("businessCategory", ""),
    "Industry Type": bp.get("businessIndustryType", ""),
    "Date of Incorporation": bp.get("dateOfIncorporation", ""),
    "Number of Employees": str(bp.get("numberOfEmployees", "")),
}

for k, v in header_checks.items():
    html_el = soup.find(string=re.compile(re.escape(v), re.I)) if v else None
    match = "✅" if (v and html_el) else ("⚠️  JSON empty" if not v else "❌ NOT in HTML")
    print(f"  {match}  {k}: {v!r}")

# Section 2: CMR Rank + Credit Vision
print()
print("=" * 70)
print("SECTION 2: RANK & CREDIT VISION METRICS")
print("=" * 70)
for r in es.get("rankSec", {}).get("rankVec", []):
    val = r.get("rankValue", "")
    found = bool(soup.find(string=re.compile(re.escape(val), re.I))) if val else False
    mark = "✅" if found else "❌"
    print(f"  {mark}  {r.get('rankName','')}: {val}")

for cv in es.get("creditVisionSec", {}).get("creditVisionVec", []):
    for algo in cv.get("cvAlgos", []):
        desc = algo.get("creditVisionDesc", "")
        val = str(algo.get("creditVisionValue", ""))
        # extract the code (e.g. AT20S) and look for it in HTML
        code = desc.split("-")[0].strip() if "-" in desc else desc
        html_code = bool(soup.find(string=re.compile(re.escape(code), re.I)))
        html_val = bool(soup.find(string=re.compile(r'\b' + re.escape(val) + r'\b')))
        mark = "✅" if (html_code and html_val) else ("⚠️  code found, val missing" if html_code else "❌")
        print(f"  {mark}  {code}: {val}")

# Section 3: Enquiry Summary
print()
print("=" * 70)
print("SECTION 3: ENQUIRY SUMMARY")
print("=" * 70)
enq_total = es.get("enquirySummarySec", {}).get("enquiryTotal", {}).get("noOfEnquiries", {})
enq_map = {
    "month1": "2", "month2to3": "9", "month4to6": "11",
    "month7to12": "13", "month13to24": "14", "greaterThan24Month": "45", "total": "94"
}
for key, expected_val in enq_map.items():
    actual = str(enq_total.get(key, ""))
    html_has = bool(soup.find(string=re.compile(r'\b' + re.escape(actual) + r'\b')))
    mark = "✅" if (html_has and actual == expected_val) else "❌"
    print(f"  {mark}  Enquiries {key}: JSON={actual!r}  HTML has value={html_has}")

# Section 4: Borrower ID details
print()
print("=" * 70)
print("SECTION 4: BORROWER ID DETAILS")
print("=" * 70)
bid = es.get("borrowerProfileSec", {}).get("borrowerIDDetails", {})
for id_rec in bid.get("idDetailsVec", {}).get("idDetails", []):
    id_val = id_rec.get("idNumber", "")
    found = bool(soup.find(string=re.compile(re.escape(id_val), re.I))) if id_val else False
    mark = "✅" if found else "❌"
    print(f"  {mark}  ID {id_rec.get('idType','')} = {id_val!r}")

# Section 5: Address
print()
print("=" * 70)
print("SECTION 5: ADDRESS & CONTACT")
print("=" * 70)
contact = es.get("borrowerProfileSec", {}).get("borrowerAddressContactDetails", {})
for field, val in contact.items():
    if not val:
        continue
    found = bool(soup.find(string=re.compile(re.escape(str(val)), re.I)))
    mark = "✅" if found else "❌"
    print(f"  {mark}  {field}: {val!r}")

# Section 6: Credit Account Summary
print()
print("=" * 70)
print("SECTION 6: CREDIT ACCOUNTS (first 5)")
print("=" * 70)
accounts = cc.get("creditAccountSummarySec", {})
# Try to find accounts list
accs = []
def find_accounts(obj):
    if isinstance(obj, list):
        for item in obj:
            if isinstance(item, dict) and ("creditFacilityType" in item or "lenderName" in item):
                accs.append(item)
            else:
                find_accounts(item)
    elif isinstance(obj, dict):
        for v in obj.values():
            find_accounts(v)

find_accounts(cc)
print(f"  Found {len(accs)} credit account records in JSON")
for acc in accs[:5]:
    name = acc.get("lenderName", acc.get("memberName", ""))
    cf_type = acc.get("creditFacilityType", acc.get("facilityType", ""))
    amount = str(acc.get("originalAmount", acc.get("sanctionedAmount", "")))
    status = acc.get("accountStatus", "")
    found_name = bool(soup.find(string=re.compile(re.escape(name), re.I))) if name else False
    print(f"  {'✅' if found_name else '❌'}  Lender={name!r}, Type={cf_type!r}, Amount={amount!r}, Status={status!r}")

print()
print("=" * 70)
print("SUMMARY")
print("=" * 70)
print(f"  JSON total fields : {len(flat_json)}")
print(f"  Credit accounts   : {len(accs)}")
print()
print("  Legend: ✅ = Value found in both HTML and JSON")
print("          ❌ = Value mismatch / not found in HTML")
print("          ⚠️  = Partial match or JSON field empty")

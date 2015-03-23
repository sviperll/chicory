$2 == "next" {
  if ($3 == "development")
    print $1 "-successor-SNAPSHOT"
  else
    print_next_versions("", $1, "")
}

$2 == "release" {
  print_release_versions("", $1, "")
}

$2 == "deeper" {
  print_n_deeper_versions($3 + 0, "", $1, "")
}

function print_release_versions(prefix, version, suffix,    v)
{
  if (match(version, /^(.*)(-successor-SNAPSHOT)$/, v))
    print_next_versions(prefix, v[1], suffix)
  else if (match(version, /^(.*)-SNAPSHOT$/, v))
    print_n_deeper_versions(2, prefix, v[1], suffix)
  else
    print_n_deeper_versions(2, prefix, version, suffix)
}

function print_next_versions(prefix, version, suffix)
{
  print_n_deeper_next_versions(1, prefix, version, suffix)
  print_next_versions_up_to_top(2, prefix, version, suffix)
}


function print_direct_next_versions(n, prefix, version, suffix,   v)
{
  if (match(version, /^(.*)alpha$/, v)) {
    print_n_deeper_versions(n, prefix, v[1] "beta", suffix)
  } else if (match(version, /^(.*)beta$/, v)) {
    print_n_deeper_versions(n, prefix, v[1] "rc", suffix)
  } else if (match(version, /^(.*[^.-])[.-]?rc$/, v)) {
    print_n_deeper_versions(n, prefix, v[1], suffix)
  } else if (match(version, /^(.*)([0-9]+)$/, v)) {
    if (!(version ~ /[^0-9.-]+/))
      print_n_deeper_versions(n, prefix, v[1] (v[2] + 1) "-alpha", suffix)
    print_n_deeper_versions(n, prefix, v[1] (v[2] + 1), suffix)
  }
}

function print_next_versions_up_to_top(n, prefix, version, suffix,   v)
{
  gsub(/[.-]?([0-9]*|[^0-9.-]*)$/, "", version)
  if (version != "") {
    print_direct_next_versions(n, prefix, version, suffix)
    print_next_versions_up_to_top(n, prefix, version, suffix)
  }
}

function print_n_deeper_versions(n, prefix, version, suffix,     v)
{
  if (n >= 0) {
    if (match(version, /^(.*[0-9])([.-]?[^0-9.-]+([.-]?[0-9].*)?)$/, v)) {
      print_n_deeper_versions(n, prefix, v[1], v[2] suffix)
      if (version ~ /^.*[0-9][.-]?[^0-9.-]+$/)
        print_n_deeper_versions(n - 1, prefix, version "-1", suffix)
    } else {
      print prefix version suffix
      if (version ~ /[0-9]+$/)
        print_n_deeper_versions(n - 1, prefix, version ".0", suffix)
    }
  }
}

function print_n_deeper_next_versions(n, prefix, version, suffix)
{
  print_n_deeper_next_versions_with_total(n, n, prefix, version, "", suffix)
}
function print_n_deeper_next_versions_with_total(total, n, prefix, version, version_suffix, suffix,    v)
{
  if (n >= 0) {
    if (match(version, /^(.*[0-9])([.-]?[^0-9.-]+([.-]?[0-9].*)?)$/, v)) {
      print_n_deeper_next_versions_with_total(total, n, prefix, v[1], v[2] version_suffix, suffix)
      if (version ~ /^.*[0-9][.-]?[^0-9.-]+$/)
        print_n_deeper_next_versions_with_total(total, n - 1, prefix, version "-1", version_suffix, suffix)
    } else {
      print_direct_next_versions(total, prefix, version version_suffix, suffix)
      if (version ~ /[0-9]+$/)
        print_n_deeper_next_versions_with_total(total, n - 1, prefix, version ".0", version_suffix, suffix)
    }
  }
}

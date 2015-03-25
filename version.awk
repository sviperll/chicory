{
  print_next_versions($1, $2)
}

function print_next_versions(version, kind,        v, s, n, numbers, numbers_separator, suffix1, suffix1_separator, suffix2, suffix2_separator, suffix3, suffix3_separator) {
  if (kind == "all") {
    print_next_versions(version, "alpha")
    print_next_versions(version, "beta")
    print_next_versions(version, "rc")
    print_next_versions(version, "")
  } else {
    s = version
    s = tokenize(s, v)
    numbers = v["token"]
    numbers_separator = numbers == "" ? "" : v["sep"]
    s = tokenize(s, v)
    suffix1 = v["token"]
    suffix1_separator = suffix1 == "" ? "" : v["sep"]
    s = tokenize(s, v)
    suffix2 = v["token"]
    suffix2_separator = suffix2 == "" ? "" : v["sep"]
    s = tokenize(s, v)
    suffix3 = v["token"]
    suffix3_separator = suffix3 == "" ? "" : v["sep"]

    kind = canonicalize(kind)

    if (isless(suffix1, kind) || (canonicalize(suffix1) == kind && (suffix2 == "" || isless(suffix2, "")))) {
      print_with_another_suffix(numbers_separator numbers, suffix1_separator, suffix1, kind)
    } else if (canonicalize(suffix1) == kind && isnumbers(suffix2)) {
      if (suffix3 == "" || isless(suffix3,""))
        print numbers_separator numbers suffix1_separator suffix1 suffix2_separator suffix2
      else {
        n = split(suffix2, v, /[.]/)
        if (n >= 3 && v[1] == "")
          print numbers_separator numbers suffix1_separator suffix1 suffix2_separator v[1] "." v[2] "." (v[3] + 1)
        if (n >= 2)
          print numbers_separator numbers suffix1_separator suffix1 suffix2_separator v[1] "." (v[2] + 1)
        print numbers_separator numbers suffix1_separator suffix1 suffix2_separator (v[1] + 1)
      }
    } else if (isnumbers(numbers)) {
      split(numbers, v, /[.]/)
      print_with_another_suffix(numbers_separator v[1] "." v[2] "." (v[3] + 1), suffix1_separator, suffix1, kind)
      print_with_another_suffix(numbers_separator v[1] "." (v[2] + 1) ".0", suffix1_separator, suffix1, kind)
      print_with_another_suffix(numbers_separator v[1] "." (v[2] + 1), suffix1_separator, suffix1, kind)
      print_with_another_suffix(numbers_separator (v[1] + 1) ".0.0", suffix1_separator, suffix1, kind)
      print_with_another_suffix(numbers_separator (v[1] + 1) ".0", suffix1_separator, suffix1, kind)
    }
  }
}

function print_with_another_suffix(s, sep, old_suffix, new_suffix)
{
  if (new_suffix == "")
    print s
  else {
    sep = old_suffix == "" ? "-" : sep
    print s sep new_suffix
    print s sep new_suffix "-1"
  }
}

function canonicalize(suffix)
{
  suffix = tolower(suffix)
  suffix = suffix == "final" ? "" : suffix
  return suffix
}

function isless(s1, s2)
{
  s1 = canonicalize(s1)
  s2 = canonicalize(s2)
  return (s1 == "alpha" && s2 == "beta") ||
         (s1 == "alpha" && s2 == "rc") ||
         (s1 == "alpha" && s2 == "snapshot") ||
         (s1 == "alpha" && s2 == "") ||
         (s1 == "beta" && s2 == "rc") ||
         (s1 == "beta" && s2 == "snapshot") ||
         (s1 == "beta" && s2 == "") ||
         (s1 == "rc" && s2 == "snapshot") ||
         (s1 == "rc" && s2 == "") ||
         (s1 == "snapshot" && s2 == "")
}

function isnumbers(s)
{
  return s ~ /^[0-9.]+$/
}

function tokenize(s, v)
{
  if (match(s, /^([-]?)([^0-9.-]+)(([0-9.-].*)?)$/, v)) {
    v["sep"] = v[1]
    v["token"] = v[2]
    s = v[3]
  } else if (match(s, /^([-]?)([0-9.]+)(([^0-9.].*)?)$/, v)) {
    v["sep"] = v[1]
    v["token"] = v[2]
    s = v[3]
  }
  return s
}


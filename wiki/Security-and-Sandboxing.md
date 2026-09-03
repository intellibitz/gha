# Security & Sandboxing

## Sandboxed Dependencies
All Gradle user home caches, toolchain downloads, and execution state are confined to `.gha/gradle-user-home/`. Zero changes are made to `~/.gradle/`.

## Secret Isolation
GitHub tokens are resolved via `GITHUB_TOKEN`, `GH_TOKEN`, or `gh auth token` dynamically and masked (`ghp_...cdef`) in all task output.
# Atlas Gradle plugin

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/gg.getatlas.publish)](https://plugins.gradle.org/plugin/gg.getatlas.publish)

[Atlas](https://getatlas.gg) is a control panel and autoscaler for Minecraft
networks. You run the panel on your own server, and it starts game servers and
proxies from templates.

This plugin sends the files your build makes, such as a Paper plugin jar, into
templates on your Atlas panel. It works with any Atlas panel of the same minor
version or the one after.

## Setup

```kotlin
plugins {
    id("gg.getatlas.publish") version "<version>"
}

atlas {
    tag = providers.environmentVariable("GITHUB_REF_NAME")
    publish("games/duels", "plugins/Duels.jar", tasks.jar)
}
```

Each `publish` line names a template on the panel, the path the file gets in
that template, and the task that builds it. Add one line per file.

The plugin reads the panel's address from `ATLAS_PANEL` and the token from
`ATLAS_TOKEN`. You can also set `panel` and `token` in the `atlas` block.

## Tasks

`./gradlew atlasPublish` builds the files and releases them under `tag`. A tag
like `v1.4.2` gives each template a new version. A tag with a dash, like
`v1.5.0-beta.1`, opens a draft per template instead. Set `draft = true` to
open drafts for a stable tag too, and `link` to the commit the release came
from.

`./gradlew atlasDev` sends the build into your own draft while you work on it.
Add `dev { server = true }` to also start or restart the draft's dev server.
It needs a personal token from **Profile**, **Tokens** in the panel.

## Documentation

The [releasing guide](https://getatlas.gg/docs/releasing) covers tokens, CI
setup and dev servers in full.

## Source

This repository mirrors the plugin from the Atlas repository on each release.

## Licence

MIT. See [LICENSE](LICENSE).

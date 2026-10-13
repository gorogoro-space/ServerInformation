# ServerInformation
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/ServerInformation.svg)](https://github.com/gorogoro-space/ServerInformation/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/ServerInformation/issues)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/gorogoro-space/ServerInformation/blob/main/LICENSE)

This plugin periodically outputs server information to the console log.

ワールドごとのチャンク数・エンティティ数・タイルエンティティ数、オンラインのプレイヤー数(統合版と Java 版の合計)、TPS を、一定間隔でコンソールのログに出すプラグインです。TPS が下がったときは spark のプロファイラを自動で実行します。

# Requirements
- Paper 1.21.11 以降(1.21.11 未満のサーバーでは読み込まれません)
- Java 21

# Installation method
Please place the .jar file in the Paper plugins folder.

.jar ファイルを Paper の plugins フォルダに置き、サーバーを再起動してください。

# Features
- `interval-seconds` 秒ごとに、ワールドごとに次の形式でログを出します
  ```
  ServerInformation: <ワールド名> <チャンク数> <エンティティ数> <タイルエンティティ数>
  ```
- 続けて、オンラインのプレイヤー数(統合版と Java 版の合計)をログに出します
  ```
  ServerInformation: players <プレイヤー数>
  ```
- 続けてコンソールから `tps` コマンドを実行し、TPS をログに出します
- TPS(1 分平均)がしきい値以下になると、コンソールから次のコマンドを実行して [spark](https://spark.lucko.me/) のプロファイラを開始します(自動プロファイル)
  ```
  spark profiler start --timeout <計測秒数> --save-to-file --only-ticks-over <ms>
  ```
  - 結果は spark が `plugins/spark/` にファイルとして保存します(アップロードはしません)
  - 停止は spark が `--timeout` で自動的に行います。このプラグインからは stop を送らないので、手動で実行中の計測を止めることはありません(手動の計測中は、spark が自動プロファイルの開始を断ります)
  - 開始するときに `ServerInformation: TPS <TPS> <= <しきい値>, <実行したコマンド>` をログに出します
  - 計測が終わってからクールダウンの時間が過ぎるまでは、再実行しません

# Config
`plugins/ServerInformation/config.yml`
- `interval-seconds` — ログを出す間隔(秒)。既定は 240
- `auto-profiler.enabled` — 自動プロファイルを使うか。既定は `true`
- `auto-profiler.tps-threshold` — この TPS(1 分平均)以下で開始する。既定は 19.0
- `auto-profiler.check-interval-seconds` — TPS を確認する間隔(秒)。既定は 20
- `auto-profiler.duration-seconds` — 計測時間(秒、spark の `--timeout`)。既定は 300
- `auto-profiler.only-ticks-over-ms` — この時間(ms)より長くかかった tick だけを記録する(spark の `--only-ticks-over`)。既定は 50
- `auto-profiler.cooldown-minutes` — 計測が終わってから次に開始できるまでの時間(分)。既定は 30
- `auto-profiler.startup-delay-minutes` — 起動直後に TPS を確認しない時間(分)。既定は 5

config.yml が既にある場合、足りない項目は起動時に既定値で追記されます。

# Disclaimer
Do not assume any responsibility by use. Please use it at your own risk.

## ビルド

JDK 21 が必要です。Gradle はラッパー(`gradlew`)が自動でダウンロードするので、別途インストールする必要はありません。

```
gradlew.bat clean build
```

(macOS / Linux では `./gradlew clean build`)

## IntelliJ IDEA でのビルド手順

本プロジェクトはビルドツールに Gradle を使用しています。
IntelliJ IDEA 上で正しくプラグイン（JARファイル）を生成するには、以下の手順を実行してください。
「ビルド → アーティファクトのビルド」はクラスファイルが入らないことがあるので使わないでください。

### 🛠️ ビルド手順

1. IntelliJ IDEA の画面右端にある **「Gradle」タブ** をクリックして開きます。
2. プロジェクト名（ServerInformation）を展開し、 **`Tasks`** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します（古いビルドキャッシュを削除します）。
4. 続けてリスト内にある **`build`** をダブルクリックして実行します。

### 📦 生成されたファイルの場所
ビルドが成功すると、プロジェクトのルート直下に `build/libs` フォルダが作成（または更新）され、その中に中身の詰まった正しい JAR ファイルが生成されます。

* **生成先:** `build/libs/ServerInformation-<version>.jar`(例: `ServerInformation-1.4.jar`。バージョンは `gradle.properties` の `version`)

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。

## 開発(IntelliJ IDEA で Claude Code を使う)

このリポジトリには、AI コーディングツール [Claude Code](https://docs.claude.com/ja/docs/claude-code/overview) 向けの作業ルールを書いた `CLAUDE.md` があります。IntelliJ IDEA で使う手順は次のとおりです。

1. Claude の有料プラン(Pro / Max)か、Anthropic API のアカウントを用意します。
2. Windows では、先に [Git for Windows](https://git-scm.com/downloads/win) をインストールします。
3. Claude Code 本体をインストールします。Windows では PowerShell で次を実行します。
   ```
   irm https://claude.ai/install.ps1 | iex
   ```
4. IntelliJ IDEA の「設定 → プラグイン → Marketplace」で「Claude Code」を検索してインストールし、IDE を再起動します。検索結果には Anthropic 以外が作った似た名前のプラグインも表示されます。プラグイン名の下に書かれた提供元が「Anthropic」になっているものを選んでください。
5. プロジェクトを開いて、ターミナルで `claude` を実行します(`Ctrl+Esc` でも起動できます)。初回はブラウザでログインします。

起動すると `CLAUDE.md` が自動で読み込まれ、このプロジェクトの設計方針に沿って作業します。

## ライセンス

[LICENSE](LICENSE) を参照してください。

## kubotan へのメモ

リポジトリを新規作成したら、**必ず Watch を設定すること**(忘れない!)。
GitHub の自動 Watch 機能は 2025 年 5 月に廃止されたため、設定しないと他の人が立てた issue や PR の通知が届かない。

1. リポジトリのページ右上の **「Watch」** を押す
2. **「Custom」** を選び、**Issues** と **Pull requests** にチェックを入れる

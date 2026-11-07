# ServerInformation

一定間隔で、ワールドごとのチャンク数・エンティティ数・タイルエンティティ数、オンラインのプレイヤー数、TPS をコンソールのログに出し、TPS が下がったら spark のプロファイラを自動で実行する Paper 用プラグイン(Paper 1.21.11 以降・Java 21 必須)。
リポジトリ: https://github.com/gorogoro-space/ServerInformation (GPL-3.0)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。新しく書くコードのコメントは日本語でよい。既存の英語のコメントは、頼まれない限り書き換えない。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。
- リポジトリへの初回の `git push` の前には、「GitHub で Watch 設定(Custom → Issues と Pull requests)はしましたか?」と日本語で確認する。自動 Watch は廃止されており、設定しないと issue や PR の通知が届かない。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外済み)。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、パスの扱い、コマンドに埋め込む文字列の検証など)
  - 安全面(データの破損・消失、再読み込み時の後始末、他プラグインの妨げなど)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## 環境

- Paper 1.21.11(`io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`、`compileOnly`)/ `plugin.yml` の `api-version: '1.21.11'`(1.21.11 未満のサーバーでは読み込まれない)
- **Java 21 必須。** `build.gradle.kts` の toolchain で JDK 21 を使い、Java 21 の class ファイルを出力する。勝手に変えない
- ビルド: Gradle(Kotlin DSL、wrapper 9.7.1)。`gradlew.bat clean build`(Windows)。**JDK 21 で実行する**(`JAVA_HOME` を `C:\Program Files\Java\jdk-21` にする。既定の `java` は JDK 27)。「ビルドして」と言われたら常にクリーンビルドする。成果物は `build/libs/ServerInformation-<version>.jar`
  - IntelliJ の「アーティファクトのビルド」はクラスファイルが入らないことがある。必ず Gradle でビルドする
- バージョンは `gradle.properties` の `version` だけで管理する。`plugin.yml` の `version: '${version}'` は `processResources` でビルド時に置き換わる
- パッケージ: `space.gorogoro.serverinformation`(**すべて小文字**。大文字が混ざると plugin.yml の main と一致せず起動しない)
- 動作確認は、サーバーの再起動でも PlugManX の `/plugman reload ServerInformation` でもよい(コマンドや権限が無いので、PlugManX で登録が不完全になる問題が起きない。定期タスクは無効化時に Bukkit が止める)。jar を差し替えるときは、どちらの手順でも先に古い jar を消す(同じ名前のプラグインが 2 つあると、どちらが読まれるかわからない)

## 最重要の設計方針

### TPS に影響させない
- このプラグインの定期タスクはメインスレッドで動く。1 回あたりの処理を軽く保つ(以前は約 930ms かかっていたのを約 4ms に改善した。重い集計を足さない)
- 高頻度イベント(PlayerInteractEvent、PlayerMoveEvent、AsyncPlayerChatEvent など)を使うときは、安い判定を先に行い、対象外なら即座に抜ける
- BlockPhysicsEvent、VehicleMoveEvent など発生頻度が極端に高いイベントは使わない
- メインスレッドでファイルや DB の同期 I/O をしない(起動時・リロード時に一度だけ行う小さなファイルの読み込みは除く)
- 設定ファイルを追加する場合は、起動時・リロード時に一度だけ解析して保持する
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く

### データの保存
- プラグインフォルダ以外には何も書き込まない

### 他プラグインとの関係
- 既存の機能を妨げないこと。イベントをキャンセルする範囲は必要最小限にする

## 機能仕様

(現在の実装の動作。仕様を変えたらここを更新する)

- **起動時(`onEnable`)**: `config.yml` が無ければ jar 内のものを保存する。既にあって足りない項目があれば、`copyDefaults(true)` と `saveConfig()` で jar 内の既定値を追記する(足りない項目が無ければ書き込まない)。`interval-seconds`(既定 240)を読む
- **定期タスク**: 起動直後から `interval-seconds` 秒ごとに、メインスレッドで次を行う
  - ワールドごとに `ServerInformation: <ワールド名> <チャンク数> <エンティティ数> <タイルエンティティ数>` をログに出す
  - `ServerInformation: players <オンラインの人数>` をログに出す(`getOnlinePlayers().size()`。統合版と Java 版を区別しない合計で、Geyser/Floodgate 経由の統合版も含まれる)
  - コンソールから `tps` コマンドを実行する(`plugin.yml` の `softdepend: [Essentials, spark]`)
- **自動プロファイル(`startAutoProfiler`)**: `auto-profiler.enabled` が true のとき、起動から `startup-delay-minutes` 分後(既定 5)から `check-interval-seconds` 秒ごと(既定 20)に、メインスレッドで `Bukkit.getTPS()[0]`(1 分平均)を確認する
  - `tps-threshold`(既定 19.0)以下なら、コンソールから `spark profiler stop --save-to-file` を実行し、`ServerInformation: TPS <TPS> <= <しきい値>, <コマンド>` をログに出す。spark のバックグラウンドの記録(10ms 間隔・全 tick・直近 1 時間、前回の保存より後の分)が保存され、記録は自動で再開する。低下する前の様子を残すためで、`start` はこの記録を破棄し、`--timeout` で終わっても再開しないので使わない
  - 手動で実行中の計測があると、その計測を止めて保存する(2026-11-07 にユーザーが承認。それまでは手動の計測を止めないよう stop を送らなかった)
  - 60 秒後(`SAVE_CHECK_SECONDS`)に、非同期で `plugins/spark/` の `profile-*.sparkprofile` に検知より後のファイルがあるかを確認する。あればファイル名をログと Discord に出す
  - ファイルが無ければバックグラウンドの記録が止まっていたと判断し(手動の `--timeout` 付きの計測や `cancel` のあと、spark の設定で無効など)、`spark profiler start --save-to-file --only-ticks-over <only-ticks-over-ms>` で検知後の計測を始める。`duration-seconds`(既定 300)後にこのプラグインが `stop --save-to-file` を送り、保存してバックグラウンドの記録を再開させる。途中でサーバーを止めると、この計測は保存されない
  - 検知から `cooldown-minutes`(既定 5)が過ぎるまで再実行しない(短くしても確認までの 60 秒は再実行しない)。検知後の計測を始めたときは「計測時間 + `cooldown-minutes`」。既にある config.yml の値は書き換わらない
  - 結果のファイルは spark が `plugins/spark/` に書く(このプラグイン自身は書かない)
  - `auto-profiler.discord-webhook-url`(既定 `""`)が `https://discord.com/api/webhooks/` か `https://discordapp.com/api/webhooks/` で始まるときは、結果を Discord へ通知する(保存できたときは TPS・しきい値・ファイル名、検知後の計測を始めたときは TPS・しきい値・計測時間・ms を入れた決まった文。メンションは入れないので `allowed_mentions` は付けない)。Java 標準の `HttpClient.sendAsync` で送り、メインスレッドを止めない。失敗はステータスか例外を警告ログに出すだけで、再試行しない。URL はログに出さない。空なら通知しない。それ以外の URL は起動時に警告を出して通知しない。無効化時に `shutdownNow()` で送信中のものを打ち切る
- コマンド・権限・イベントの処理は無い(`Listener` として登録しているが、ハンドラは無い)
- 例外は `logStackTrace` でスタックトレースを警告ログに出す

## 過去にハマった点(他プロジェクトでの経験)

- `config.getString(path, "")` のように既定値を渡すと、jar 内 config.yml の既定値が参照されない。既定値なしで取得して null を判定すること
- plugin.yml で `default: true` にした権限でも、登録されないと Bukkit は「OP のみ」として扱う。全員向けの機能を権限で縛らない

## ファイル構成

- `src/main/java/space/gorogoro/serverinformation/ServerInformation.java` — メインクラス。設定の読み込みと定期タスクをすべて持つ
- `src/main/resources/plugin.yml` — プラグイン定義
- `src/main/resources/config.yml` — 出力間隔(`interval-seconds`)と自動プロファイルの設定(`auto-profiler.*`)
- `build.gradle.kts` / `settings.gradle.kts` / `gradle.properties` — Gradle の設定(バージョンは `gradle.properties`)

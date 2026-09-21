# ServerInformation
ServerInformation

## IntelliJ IDEA でのビルド手順

本プロジェクトはビルドツールに Maven を使用しています。
IntelliJ IDEA 上で正しくプラグイン（JARファイル）を生成するには、以下の手順を実行してください。

### 🚨 注意：Plugins ではなく Lifecycle を使用してください
Maven ツールウィンドウ内の `Plugins` -> `jar:jar` を直接実行すると、コンパイルが行われず中身が空の JAR ファイルが生成されてしまいます。
必ず以下の手順通り **`Lifecycle`** を使用してください。

### 🛠️ ビルド手順

1. IntelliJ IDEA の画面右端にある **「Maven」タブ** をクリックして開きます。
2. プロジェクト名（ServerInformation）を展開し、 **`Lifecycle`（ライフサイクル）** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します（古いビルドキャッシュを削除します）。
4. 続けてリスト内にある **`package`** をダブルクリックして実行します。

### 📦 生成されたファイルの場所
ビルドが成功すると、プロジェクトのルート直下に `target` フォルダが作成（または更新）され、その中に中身の詰まった正しい JAR ファイルが生成されます。

* **生成先:** `target/ServerInformation-1.1.jar`

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。


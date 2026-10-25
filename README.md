# Seçim Anketi (iPhone)

Türkiye'deki seçimler için çevrim içi, canlı ve **temsili** bir anket uygulaması. Tüm partiler, liderler ve rakamlar uydurmadır.

- Proje brifi ve onaylanan kararlar: [`CLAUDE.md`](CLAUDE.md)
- Tasarım referansları: [`design-reference/v2-onaylanan`](design-reference/v2-onaylanan)
- Son ekran görüntüleri: [`docs/screenshots/latest`](docs/screenshots/latest)

## Projeyi açmak (Mac)

```sh
brew install xcodegen
xcodegen generate
open SecimAnketi.xcodeproj
```

SwiftUI, yalnızca iPhone, en az iOS 17.

## Yapı

```
App/                    Uygulama hedefi (SwiftUI)
  Features/Gallery/     Aşama 1 bileşen galerisi
Packages/PollCore/      Modeller, örnek veri, mum ölçeği, şerit geometrisi (arayüzsüz)
Packages/DesignSystem/  Renk/yazı token'ları, PartyLogo, VoteCandlePair
Tests/                  Görüntü testleri ve simülatörde arayüz testleri
tools/                  Örnek veri üreticisi
```

## Örnek veri

`Packages/PollCore/Sources/PollCore/Resources/mock-data.json` dosyası elle düzenlenmez:

```sh
python3 tools/generate_mock_data.py
```

## CI

`.github/workflows/ios.yml` her push'ta PollCore testlerini çalıştırır, uygulamayı derler, görüntü ve arayüz testlerini simülatörde koşturur. Commit mesajında `[ekran]` varsa ekran görüntüleri ve ekran kaydı `docs/screenshots/latest/` altına yazılır.

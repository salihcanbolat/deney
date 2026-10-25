import SwiftUI
import DesignSystem
import PollCore

/// Aşama 1 bileşen galerisi. Ekran görüntüsü testleri de `GalleryContent`'i doğrudan çizer.
struct DesignGalleryView: View {
    var body: some View {
        ScrollView {
            GalleryContent()
                .padding(.top, 11)
                .padding(.bottom, 32)
        }
        .background(Palette.groupedBackground.ignoresSafeArea())
    }
}

struct GalleryContent: View {
    var data: MockDataset = .standard
    var animated = true

    var body: some View {
        let sample = StripSample(data)
        VStack(alignment: .leading, spacing: 0) {
            Text("Bileşenler")
                .font(AppFont.largeTitle)
                .tracking(-0.4)
                .foregroundStyle(Palette.label)
                .padding(.leading, 4)
                .padding(.bottom, 2)
            Text("Aşama 1 · Temsili anketler · örnek veri")
                .font(AppFont.footnote)
                .foregroundStyle(Palette.secondaryText)
                .padding(.leading, 4)
                .padding(.bottom, 22)

            VStack(alignment: .leading, spacing: 26) {
                GroupedSection(title: "CANLI ÖNİZLEME", footer: "Dinlenme → basılı tut → parmağı kaydır → bırak. Gerçek jest Aşama 2'de.") {
                    CandleDemoCard(sample: sample, animated: animated)
                }
                GroupedSection(title: "MUM ÇİFTİ", footer: "Sol mum parti, sağ mum lider. Tüm mumlar aynı ölçeği kullanır (tavan %\(Int(sample.scale.ceiling))).") {
                    CandleStatesCard(sample: sample)
                }
                GroupedSection(title: "PARTİ LOGOSU", footer: "Şimdilik parti renginde daire + kodun ilk harfi. Gerçek logo geldiğinde yalnızca veri değişir.") {
                    LogoShowcase(parties: sample.columns.map(\.party))
                }
                GroupedSection(title: "RENKLER", footer: "Soldaki kutu açık mod, sağdaki koyu mod önerisi.") {
                    TokenList()
                }
                GroupedSection(title: "KOYU MOD ÖNERİSİ", footer: "Koyu modda yazı ve ikonlar daha açık bir mürekkep (ink), düğmeler daha koyu bir mürekkep (inkFill) kullanır; beyaz yazı okunur kalır.") {
                    DarkModeProposal()
                }
                GroupedSection(title: "YAZI") {
                    TypeRamp()
                }
            }
        }
        .padding(.horizontal, Metrics.gutter)
    }
}

#Preview {
    DesignGalleryView()
}

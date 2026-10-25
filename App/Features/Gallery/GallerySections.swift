import SwiftUI
import DesignSystem
import PollCore

// MARK: - Canlı önizleme

/// Şeridin kendi kendine oynayan önizlemesi: dinlenme → basılı tut → parmağı kaydır → bırak.
/// Gerçek jest kilit ekranında; burada yalnızca animasyonların hissi görülür.
struct CandleDemoCard: View {
    let data: StripData
    var animated = true

    @State private var held = false
    @State private var activeIndex = 0

    var body: some View {
        LockBackdrop {
            StripView(data: data, held: $held, activeIndex: $activeIndex, interactive: false)
        }
        .task(id: animated) {
            guard animated else { return }
            await play()
        }
    }

    private func play() async {
        // (basılı mı, sütun, bekleme süresi)
        let steps: [(Bool, Int, Double)] = [
            (false, 0, 1.4),
            (true, 0, 1.0),
            (true, 1, 0.28), (true, 2, 1.0),
            (true, 3, 0.24), (true, 4, 0.24), (true, 5, 1.0),
            (true, 4, 0.24), (true, 3, 0.24), (true, 2, 0.9),
            (false, 2, 1.6),
        ]
        while !Task.isCancelled {
            for (held, column, pause) in steps {
                withAnimation(Motion.candle) {
                    self.held = held
                    activeIndex = column
                }
                try? await Task.sleep(for: .seconds(pause))
                if Task.isCancelled { return }
            }
        }
    }
}

// MARK: - Mum çifti

struct CandleStatesCard: View {
    let data: StripData

    var body: some View {
        LockBackdrop {
            VStack(spacing: 12) {
                caption("Dinlenme · 34 pt alan, 7 pt mum")
                StripView(data: data, held: .constant(false), activeIndex: .constant(0), interactive: false)
                caption("Basılı tutma · 92 pt artı, 70 pt eksi alan, 10 pt mum")
                    .padding(.top, 6)
                StripView(data: data, held: .constant(true), activeIndex: .constant(2), interactive: false)
            }
        }
    }

    private func caption(_ text: String) -> some View {
        Text(text)
            .font(LockFont.hint)
            .foregroundStyle(Color.white.opacity(0.62))
    }
}

// MARK: - Parti logosu

struct LogoShowcase: View {
    let parties: [Party]

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 0) {
                ForEach(parties) { party in
                    VStack(spacing: 4) {
                        PartyLogo(party, size: 40)
                        Text(party.code)
                            .font(.system(size: 13, weight: .bold))
                            .foregroundStyle(Palette.label)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            .padding(.vertical, 14)
            .padding(.horizontal, 6)

            Hairline()

            if let first = parties.first {
                HStack(alignment: .bottom, spacing: 22) {
                    sizeSample(first, size: 40, label: "40 pt · pusula")
                    sizeSample(first, size: 26, label: "26 pt · şerit")
                    sizeSample(first, size: 20, label: "20 pt · önizleme")
                    Spacer(minLength: 0)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }

            LockBackdrop {
                HStack(spacing: 0) {
                    ForEach(Array(parties.enumerated()), id: \.element.id) { index, party in
                        PartyLogo(party, size: StripMetrics.logoSize, border: index == 2 ? .focusRing : .hairline)
                            .scaleEffect(index == 2 ? StripMetrics.activeLogoScale : 1)
                            .frame(maxWidth: .infinity)
                    }
                }
                .frame(width: StripMetrics.width - 16)
            }
        }
    }

    private func sizeSample(_ party: Party, size: CGFloat, label: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            PartyLogo(party, size: size)
            Text(label)
                .font(AppFont.caption)
                .foregroundStyle(Palette.secondaryText)
        }
    }
}

// MARK: - Renkler

struct TokenList: View {
    struct Token: Identifiable {
        let name: String
        let color: Color
        let light: String
        let dark: String?
        var id: String { name }
    }

    static let tokens: [Token] = [
        Token(name: "ink", color: Palette.ink, light: "#3F3596", dark: "#9D95F0"),
        Token(name: "inkFill", color: Palette.inkFill, light: "#3F3596", dark: "#5A4FC4"),
        Token(name: "inkTint", color: Palette.inkTint, light: "#ECEAF7", dark: "#26224A"),
        Token(name: "voteCandle", color: Palette.voteCandle, light: "#F3EBDD", dark: nil),
        Token(name: "lockWallpaper", color: Palette.lockWallpaper, light: "#161C27", dark: nil),
        Token(name: "groupedBg", color: Palette.groupedBackground, light: "#F2F2F7", dark: "#000000"),
        Token(name: "card", color: Palette.card, light: "#FFFFFF", dark: "#1C1C1E"),
        Token(name: "label", color: Palette.label, light: "#1C1C1E", dark: "#FFFFFF"),
        Token(name: "secondaryText", color: Palette.secondaryText, light: "#6D6D72", dark: "#98989F"),
        Token(name: "separator", color: Palette.separator, light: "#D8D8DD", dark: "#38383A"),
        Token(name: "paper", color: Palette.paper, light: "#FBF8F1", dark: "#ECE7DB"),
        Token(name: "paperLine", color: Palette.paperLine, light: "#D2C8B3", dark: nil),
        Token(name: "paperInk", color: Palette.paperInk, light: "#2E2A22", dark: nil),
        Token(name: "kraft", color: Palette.kraft, light: "#D6BC8E", dark: nil),
        Token(name: "switchOn", color: Palette.switchOn, light: "#34C759", dark: "#30D158"),
    ]

    var body: some View {
        VStack(spacing: 0) {
            ForEach(Array(Self.tokens.enumerated()), id: \.element.id) { index, token in
                HStack(spacing: 10) {
                    swatch(token.color)
                        .environment(\.colorScheme, .light)
                    swatch(token.color)
                        .environment(\.colorScheme, .dark)
                    VStack(alignment: .leading, spacing: 1) {
                        Text(token.name)
                            .font(.system(.subheadline, design: .monospaced).weight(.semibold))
                            .foregroundStyle(Palette.label)
                        Text(token.dark.map { "\(token.light) · \($0)" } ?? "\(token.light) · moddan bağımsız")
                            .font(AppFont.caption)
                            .monospacedDigit()
                            .foregroundStyle(Palette.secondaryText)
                    }
                    .padding(.leading, 4)
                    Spacer(minLength: 0)
                }
                .padding(.horizontal, 16)
                .frame(minHeight: Metrics.settingRowMinHeight)
                .overlay(alignment: .bottom) {
                    if index < Self.tokens.count - 1 { Hairline(leadingInset: 16) }
                }
            }
        }
    }

    private func swatch(_ color: Color) -> some View {
        RoundedRectangle(cornerRadius: 7, style: .continuous)
            .fill(color)
            .frame(width: 34, height: 26)
            .overlay {
                RoundedRectangle(cornerRadius: 7, style: .continuous)
                    .strokeBorder(Color.black.opacity(0.12), lineWidth: 0.5)
            }
    }
}

// MARK: - Koyu mod önerisi

struct DarkModeProposal: View {
    var body: some View {
        VStack(spacing: 0) {
            SampleScreen(label: "Açık · tasarım")
                .environment(\.colorScheme, .light)
            SampleScreen(label: "Koyu · öneri")
                .environment(\.colorScheme, .dark)
        }
    }
}

/// Anketler ve Oy Kabini ekranlarından küçük bir örnek; iki modda yan yana karşılaştırmak için.
private struct SampleScreen: View {
    let label: String

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(label)
                .font(AppFont.footnote)
                .foregroundStyle(Palette.secondaryText)
                .padding(.leading, 4)
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    tile(symbol: "checkmark", fill: Palette.inkFill, glyph: .white)
                    titles("Genel Seçim Anketi", "Destek + Asla · 128.460 oy")
                    Image(systemName: "bell.fill")
                        .font(.system(size: 18))
                        .foregroundStyle(Palette.ink)
                        .frame(width: Metrics.minTapTarget, height: Metrics.minTapTarget)
                    Image(systemName: "lock")
                        .font(.system(size: 17))
                        .foregroundStyle(Palette.inactiveIcon)
                        .frame(width: Metrics.minTapTarget, height: Metrics.minTapTarget)
                }
                .padding(.leading, 14)
                .padding(.trailing, 6)
                .frame(minHeight: Metrics.rowMinHeight)
                Hairline()
                HStack(spacing: 12) {
                    tile(symbol: "xmark", fill: Palette.inkTint, glyph: Palette.ink)
                    titles("Asla Oy Vermem Anketi", "Yalnız asla · 61.902 oy")
                    Text("Katıl")
                        .font(AppFont.subhead.weight(.semibold))
                        .foregroundStyle(Palette.ink)
                        .padding(.horizontal, 14)
                        .frame(height: 30)
                        .background(Palette.inkTint, in: Capsule())
                }
                .padding(.horizontal, 14)
                .frame(minHeight: Metrics.rowMinHeight)
            }
            .background(Palette.card, in: RoundedRectangle(cornerRadius: Metrics.cardRadius, style: .continuous))

            Text("Pusulayı katla ve zarfa koy")
                .font(AppFont.headline)
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .frame(height: Metrics.buttonHeight)
                .background(Palette.inkFill, in: RoundedRectangle(cornerRadius: Metrics.buttonRadius, style: .continuous))
        }
        .padding(12)
        .background(Palette.groupedBackground)
    }

    private func tile(symbol: String, fill: Color, glyph: Color) -> some View {
        RoundedRectangle(cornerRadius: 8, style: .continuous)
            .fill(fill)
            .frame(width: 32, height: 32)
            .overlay {
                Image(systemName: symbol)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(glyph)
            }
    }

    private func titles(_ title: String, _ subtitle: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title)
                .font(AppFont.rowTitle)
                .foregroundStyle(Palette.label)
                .lineLimit(1)
            Text(subtitle)
                .font(AppFont.footnote)
                .monospacedDigit()
                .lineLimit(1)
                .foregroundStyle(Palette.secondaryText)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Yazı

struct TypeRamp: View {
    private let samples: [(label: String, font: Font, text: String)] = [
        ("Large Title · 34/700", AppFont.largeTitle, "Anketler"),
        ("Title · 28/700", AppFont.title, "Oyun sandıkta"),
        ("Headline · 17/600", AppFont.headline, "Oy Kabini"),
        ("Body · 17", AppFont.body, "Kilit ekranında takip et"),
        ("Satır başlığı · 16/600", AppFont.rowTitle, "Genç Seçmen Anketi"),
        ("Subhead · 15", AppFont.subhead, "Tercihin anonim olarak eklendi."),
        ("Footnote · 13", AppFont.footnote, "Temsili anketler · örnek veri"),
        ("Caption · 12", AppFont.caption, "Desteklediğin partinin kutusuna dokun"),
        ("Caption2 · 11", AppFont.caption2, "TEMSİLİ ANKET PUSULASI"),
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ForEach(samples.indices, id: \.self) { index in
                VStack(alignment: .leading, spacing: 2) {
                    Text(samples[index].text)
                        .font(samples[index].font)
                        .foregroundStyle(Palette.label)
                    Text(samples[index].label)
                        .font(AppFont.caption)
                        .foregroundStyle(Palette.secondaryText)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .overlay(alignment: .bottom) { Hairline(leadingInset: 16) }
            }
            LockBackdrop {
                VStack(spacing: 0) {
                    Text("Pazar 25 Ekim")
                        .font(LockFont.date)
                        .foregroundStyle(Color.white.opacity(0.82))
                    Text("9:41")
                        .font(LockFont.clock)
                        .tracking(LockFont.clockTracking)
                        .monospacedDigit()
                        .foregroundStyle(Color.white.opacity(0.94))
                    Text("Sol mum parti, sağ mum lider · Basılı tut")
                        .font(LockFont.hint)
                        .foregroundStyle(Color.white.opacity(0.62))
                        .padding(.top, 6)
                }
            }
        }
    }
}

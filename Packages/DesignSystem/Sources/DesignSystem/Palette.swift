import SwiftUI

/// Renk token'ları: CLAUDE.md Bölüm 4.
///
/// Açık mod değerleri tasarımdan birebir alındı. Koyu mod değerleri öneridir; onaylanana kadar
/// uygulama ekranları açık modda sabit tutulur (`RootView`).
public enum Palette {
    // MARK: Vurgu

    /// Tek vurgu rengi: yazı, ikon, mühür, seçili sekme.
    public static let ink = Color.adaptive(light: "#3F3596", dark: "#9D95F0")
    /// Vurgu rengindeki dolgular (ana düğme, ikon zemini). Koyu modda beyaz yazının okunması için `ink`'ten koyudur.
    public static let inkFill = Color.adaptive(light: "#3F3596", dark: "#5A4FC4")
    /// Vurgunun açık zemini (pasif kapsül, ikon zemini).
    public static let inkTint = Color.adaptive(light: "#ECEAF7", dark: "#26224A")

    // MARK: Mumlar ve kilit ekranı (moddan bağımsız)

    /// Tüm mumlar. Parti renkleri mumlarda asla kullanılmaz.
    public static let voteCandle = Color(hex: "#F3EBDD")
    /// Mumun dolgusu: aynı kâğıt beyazı, soldan gelen ışıkla balmumu bir silindir gibi.
    /// Renk değişmez, yalnızca ışık düşer; parti rengi yine kullanılmaz.
    public static let voteCandleWax = LinearGradient(
        stops: [
            .init(color: Color(hex: "#FFFCF6"), location: 0),
            .init(color: Color(hex: "#F3EBDD"), location: 0.42),
            .init(color: Color(hex: "#DACDB6"), location: 1),
        ],
        startPoint: .leading,
        endPoint: .trailing
    )
    public static let lockWallpaper = Color(hex: "#161C27")
    /// Duvar kâğıdındaki büyük dairenin rengi.
    public static let lockWallpaperGlow = Color(hex: "#1A2130")
    public static let stripGlassFill = Color.white.opacity(0.10)
    public static let stripGlassStroke = Color.white.opacity(0.22)

    // MARK: Uygulama ekranları

    public static let groupedBackground = Color.adaptive(light: "#F2F2F7", dark: "#000000")
    public static let card = Color.adaptive(light: "#FFFFFF", dark: "#1C1C1E")
    public static let label = Color.adaptive(light: "#1C1C1E", dark: "#FFFFFF")
    public static let secondaryText = Color.adaptive(light: "#6D6D72", dark: "#98989F")
    /// Pasif zil ve kilit simgeleri.
    public static let inactiveIcon = Color.adaptive(light: "#AEAEB2", dark: "#636366")
    /// Pasif sekme.
    public static let inactiveTab = Color.adaptive(light: "#8E8E93", dark: "#8E8E93")
    public static let separator = Color.adaptive(light: "#D8D8DD", dark: "#38383A")
    /// Segment kontrolü ve kapalı anahtarın zemini.
    public static let fillTrack = Color.adaptive(light: "#E3E3E8", dark: "#2C2C2E")
    public static let switchOn = Color.adaptive(light: "#34C759", dark: "#30D158")

    // MARK: Pusula (fiziksel bir kâğıt; koyu modda da kâğıt kalır, yalnızca biraz kısılır)

    public static let paper = Color.adaptive(light: "#FBF8F1", dark: "#ECE7DB")
    public static let paperEdge = Color(hex: "#D9D0BC")
    public static let paperLine = Color(hex: "#D2C8B3")
    public static let paperInk = Color(hex: "#2E2A22")
    public static let paperInkSecondary = Color(hex: "#4A453B")
    public static let paperInkTertiary = Color(hex: "#6A6458")
    public static let photoSlot = Color(hex: "#EFE9DC")
    public static let photoSlotEdge = Color(hex: "#CFC6B2")

    // MARK: Zarf ve sandık

    public static let kraft = Color(hex: "#D6BC8E")
    public static let kraftFlap = Color(hex: "#CBAE7C")
    public static let kraftLight = Color(hex: "#E1CCA4")
    public static let ballotBoxLid = Color(hex: "#2C2C2E")
}

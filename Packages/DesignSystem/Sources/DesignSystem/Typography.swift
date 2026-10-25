import SwiftUI

/// Uygulama ekranlarının yazı ölçeği. iOS metin stillerine bağlıdır, Dynamic Type ile ölçeklenir.
/// Varsayılan boyutta değerler tasarımla aynıdır. En küçük boyut 11 pt'dir.
public enum AppFont {
    /// 34 / 700
    public static let largeTitle = Font.largeTitle.weight(.bold)
    /// 28 / 700
    public static let title = Font.title.weight(.bold)
    /// 17 / 600
    public static let headline = Font.headline
    /// 17 / 400
    public static let body = Font.body
    /// 16 / 600 (liste satırı başlığı)
    public static let rowTitle = Font.callout.weight(.semibold)
    /// 15
    public static let subhead = Font.subheadline
    /// 13
    public static let footnote = Font.footnote
    /// 12
    public static let caption = Font.caption
    /// 11
    public static let caption2 = Font.caption2
}

/// Kilit ekranı yazıları. Şerit sabit boyutludur, Dynamic Type ile büyümez.
public enum LockFont {
    /// 96 / 700, harf aralığı −3
    public static let clock = Font.system(size: 96, weight: .bold)
    public static let clockTracking: CGFloat = -3
    /// 17 / 600
    public static let date = Font.system(size: 17, weight: .semibold)
    /// 12 / 600: şerit başlığı ("Genel Seçim Anketi", "Canlı")
    public static let stripTitle = Font.system(size: 12, weight: .semibold)
    /// 15 / 600: basılı tutulan partinin adı
    public static let partyName = Font.system(size: 15, weight: .semibold)
    /// 12: "Lider adı · N. sıra"
    public static let leaderLine = Font.system(size: 12)
    /// 11 / 600: oran tablosu
    public static let table = Font.system(size: 11, weight: .semibold)
    /// 11 / 500: tablo başlıkları ("Parti", "Lider")
    public static let tableHeader = Font.system(size: 11, weight: .medium)
    /// 12 / 500: şeridin altındaki yardım metni
    public static let hint = Font.system(size: 12, weight: .medium)
}

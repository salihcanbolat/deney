import CoreGraphics

/// Ölçüler: CLAUDE.md Bölüm 4 ve 5.
public enum Metrics {
    public static let gutter: CGFloat = 16
    public static let cardRadius: CGFloat = 14
    public static let buttonHeight: CGFloat = 50
    public static let buttonRadius: CGFloat = 14
    public static let minTapTarget: CGFloat = 44
    public static let rowMinHeight: CGFloat = 64
    public static let settingRowMinHeight: CGFloat = 48
}

/// Kilit ekranı şeridi ölçüleri.
public enum StripMetrics {
    public static let width: CGFloat = 350
    public static let cornerRadius: CGFloat = 26
    public static let topSpacingBelowClock: CGFloat = 12

    public static let logoSize: CGFloat = 26
    public static let logoVerticalPadding: CGFloat = 5
    public static let candleGap: CGFloat = 1

    public static let restPositiveZone: CGFloat = 34
    public static let heldPositiveZone: CGFloat = 92
    public static let heldNegativeZone: CGFloat = 70
    public static let restCandleWidth: CGFloat = 7
    public static let heldCandleWidth: CGFloat = 10

    public static let activeLogoScale: CGFloat = 1.18
    public static let dimmedColumnOpacity: Double = 0.28
}

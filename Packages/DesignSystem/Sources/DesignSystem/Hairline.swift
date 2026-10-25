import SwiftUI

/// Gruplu liste ayırıcısı (0,5 pt, `Palette.separator`).
public struct Hairline: View {
    private let leadingInset: CGFloat

    public init(leadingInset: CGFloat = 0) {
        self.leadingInset = leadingInset
    }

    public var body: some View {
        Rectangle()
            .fill(Palette.separator)
            .frame(height: 0.5)
            .padding(.leading, leadingInset)
    }
}

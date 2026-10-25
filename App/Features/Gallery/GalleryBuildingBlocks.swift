import SwiftUI
import DesignSystem

/// iOS gruplu liste bölümü: büyük harfli başlık, beyaz kart, isteğe bağlı açıklama.
struct GroupedSection<Content: View>: View {
    let title: String
    var footer: String? = nil
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .font(AppFont.footnote)
                .tracking(0.2)
                .foregroundStyle(Palette.secondaryText)
                .padding(.leading, 16)
                .padding(.bottom, 7)
            content
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Palette.card)
                .clipShape(RoundedRectangle(cornerRadius: Metrics.cardRadius, style: .continuous))
            if let footer {
                Text(footer)
                    .font(AppFont.footnote)
                    .foregroundStyle(Palette.secondaryText)
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
            }
        }
    }
}

/// Kilit ekranı zemininde koyu bir alan.
struct LockBackdrop<Content: View>: View {
    @ViewBuilder var content: Content

    var body: some View {
        content
            .padding(.vertical, 14)
            .frame(maxWidth: .infinity)
            .background(Palette.lockWallpaper)
    }
}

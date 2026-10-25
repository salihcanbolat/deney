import SwiftUI
import DesignSystem

/// Uygulama içindeki kilit ekranı replikası (Ekran 1–3). Tasarımın birebir aynısı;
/// basılı tut + kaydır deneyimi burada yaşanır (Bölüm 3, seçenek A).
struct LockScreenView: View {
    let data: StripData
    var fixedDate: Date?

    @State private var held: Bool
    @State private var activeIndex: Int

    init(data: StripData, held: Bool = false, activeIndex: Int = 0, fixedDate: Date? = nil) {
        self.data = data
        self.fixedDate = fixedDate
        _held = State(initialValue: held)
        _activeIndex = State(initialValue: activeIndex)
    }

    var body: some View {
        ZStack(alignment: .top) {
            LockWallpaper()

            VStack(spacing: 0) {
                LockClock(fixedDate: fixedDate)
                StripView(data: data, held: $held, activeIndex: $activeIndex)
                    .padding(.top, StripMetrics.topSpacingBelowClock)
                Text("Sol mum parti, sağ mum lider · Basılı tut")
                    .font(LockFont.hint)
                    .foregroundStyle(Color.white.opacity(0.62))
                    .padding(.top, 10)
                    .opacity(held ? 0 : 1)
                    .animation(Motion.fade, value: held)
                    .accessibilityHidden(held)
            }
            .padding(.top, 66)

            LockQuickActions()
        }
        .ignoresSafeArea()
    }
}

/// Duvar kâğıdı: #161C27 zemin ve derinlik veren #1A2130 tonunda büyük bir daire.
struct LockWallpaper: View {
    var body: some View {
        GeometryReader { proxy in
            Palette.lockWallpaper
                .overlay {
                    Circle()
                        .fill(Palette.lockWallpaperGlow)
                        .frame(width: 790, height: 790)
                        .position(x: proxy.size.width / 2, y: 300 + 395)
                }
                .clipped()
        }
        .accessibilityHidden(true)
    }
}

/// Tarih ve saat. Varsayılan olarak gerçek saati gösterir; ekran görüntüleri için sabitlenebilir.
struct LockClock: View {
    var fixedDate: Date?

    var body: some View {
        TimelineView(.everyMinute) { context in
            let date = fixedDate ?? context.date
            VStack(spacing: 0) {
                Text(Self.day.string(from: date))
                    .font(LockFont.date)
                    .tracking(0.1)
                    .foregroundStyle(Color.white.opacity(0.82))
                Text(Self.time.string(from: date))
                    .font(LockFont.clock)
                    .tracking(LockFont.clockTracking)
                    .monospacedDigit()
                    .foregroundStyle(Color.white.opacity(0.94))
                    .contentTransition(.numericText())
                    .frame(height: 94)
            }
            .accessibilityElement(children: .combine)
        }
    }

    private static let day: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "tr_TR")
        formatter.dateFormat = "EEEE d MMMM"
        return formatter
    }()

    private static let time: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "tr_TR")
        formatter.dateFormat = "H:mm"
        return formatter
    }()
}

/// Alt köşelerdeki el feneri ve kamera düğmeleri (görsel; kilit ekranı hissi için).
struct LockQuickActions: View {
    var body: some View {
        VStack {
            Spacer()
            HStack {
                button("flashlight.off.fill")
                Spacer()
                button("camera.fill")
            }
            .padding(.horizontal, 46)
            .padding(.bottom, 46)
        }
        .accessibilityHidden(true)
    }

    private func button(_ symbol: String) -> some View {
        Image(systemName: symbol)
            .font(.system(size: 20, weight: .regular))
            .foregroundStyle(.white)
            .frame(width: 50, height: 50)
            .background {
                Circle()
                    .fill(.ultraThinMaterial)
                    .environment(\.colorScheme, .dark)
                    .overlay { Circle().fill(Color.white.opacity(0.08)) }
            }
    }
}

#Preview("Dinlenme") {
    LockScreenView(data: StripData(.standard))
}

#Preview("Basılı · BHP") {
    LockScreenView(data: StripData(.standard), held: true, activeIndex: 2)
}

import SwiftUI
import PollCore

/// Aşama 2: uygulama kilit ekranı replikasıyla açılır. Sekmeli yapı Aşama 3'te gelir.
struct RootView: View {
    var body: some View {
        if LaunchOptions.showsGallery {
            DesignGalleryView()
                // Koyu mod paleti onaylanana kadar uygulama ekranları açık modda kalır.
                .preferredColorScheme(.light)
        } else {
            LockScreenView(
                data: StripData(.standard),
                held: LaunchOptions.stripHeld,
                activeIndex: LaunchOptions.stripIndex,
                fixedDate: LaunchOptions.fixedDate
            )
            .preferredColorScheme(.dark)
        }
    }
}

/// Testlerin ve ekran görüntülerinin kullandığı başlatma argümanları (`-gallery YES` gibi).
enum LaunchOptions {
    private static var defaults: UserDefaults { .standard }

    static var showsGallery: Bool { defaults.bool(forKey: "gallery") }
    static var stripHeld: Bool { defaults.bool(forKey: "stripHeld") }
    static var stripIndex: Int { defaults.integer(forKey: "stripIndex") }

    /// `-demoDate YES`: tasarımdaki gibi "Pazar 25 Ekim, 9:41".
    static var fixedDate: Date? {
        guard defaults.bool(forKey: "demoDate") else { return nil }
        return DateComponents(calendar: .current, year: 2026, month: 10, day: 25, hour: 9, minute: 41).date
    }
}

import SwiftUI

/// Aşama 1: uygulama bileşen galerisiyle açılır. Sekmeli yapı Aşama 2–3'te gelir.
struct RootView: View {
    var body: some View {
        DesignGalleryView()
            // Koyu mod paleti onaylanana kadar uygulama ekranları açık modda kalır.
            .preferredColorScheme(.light)
    }
}

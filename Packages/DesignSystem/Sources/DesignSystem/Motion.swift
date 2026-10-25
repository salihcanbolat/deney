import SwiftUI

/// Hareket: CLAUDE.md Bölüm 4. Hepsi iOS'un kendi yay eğrileridir; parmak hareketi yarıda
/// kesilse bile animasyon bulunduğu hızdan devam eder.
public enum Motion {
    /// Mum yükseklik ve genişlikleri (~260 ms, tasarımdaki cubic-bezier(.2,.8,.2,1) karşılığı).
    public static let candle = Animation.spring(response: 0.3, dampingFraction: 0.85)
    /// Logonun büyümesi ve odak halkası (~200 ms, hafif esneme).
    public static let logoFocus = Animation.spring(response: 0.22, dampingFraction: 0.72)
    /// Sütunların sönükleşmesi (~200 ms).
    public static let fade = Animation.easeInOut(duration: 0.2)
}

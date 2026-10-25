// swift-tools-version: 5.9
import PackageDescription

// Arayüzsüz çekirdek: modeller, örnek veri, mum ölçeği ve şerit geometrisi.
// SwiftUI'ye bağlı değildir; widget ve backend katmanı da aynı paketi kullanır.
let package = Package(
    name: "PollCore",
    platforms: [.iOS(.v17), .macOS(.v14)],
    products: [
        .library(name: "PollCore", targets: ["PollCore"]),
    ],
    targets: [
        .target(name: "PollCore", resources: [.process("Resources")]),
        .testTarget(name: "PollCoreTests", dependencies: ["PollCore"]),
    ]
)

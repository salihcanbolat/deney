import Foundation

/// Paketle gelen örnek veri (`Resources/mock-data.json`, `tools/generate_mock_data.py` ile üretilir).
public struct MockDataset: Codable, Sendable {
    public struct SampleUser: Codable, Sendable {
        public let prefs: UserPrefs
        public let ballots: [Ballot]
    }

    public let parties: [Party]
    public let polls: [Poll]
    public let results: [PollResult]
    public let sampleUser: SampleUser

    /// `bundle` verilmezse paketin kendi kaynakları kullanılır.
    public static func load(from bundle: Bundle? = nil) throws -> MockDataset {
        guard let url = (bundle ?? .module).url(forResource: "mock-data", withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return try decoder.decode(MockDataset.self, from: Data(contentsOf: url))
    }

    /// Paketteki örnek veri. Dosya paketle birlikte geldiği için okunamaması bir derleme hatasıdır.
    public static let standard: MockDataset = {
        do {
            return try load()
        } catch {
            fatalError("mock-data.json okunamadı: \(error)")
        }
    }()

    public func party(_ id: Party.ID) -> Party? { parties.first { $0.id == id } }
    public func party(code: String) -> Party? { parties.first { $0.code == code } }
    public func poll(_ id: Poll.ID) -> Poll? { polls.first { $0.id == id } }
    public func result(for pollId: Poll.ID) -> PollResult? { results.first { $0.pollId == pollId } }
}

package com.annam.shop.seed;

import java.util.List;

public final class SampleCatalog {

    public record CategorySeed(String name, String description) {
    }

    public record ProductSeed(String category, String name, String description, long price) {
    }

    private SampleCatalog() {
    }

    public static List<CategorySeed> categories() {
        return List.of(
                new CategorySeed("Nhà bếp", "Đồ nấu và dụng cụ dùng mỗi ngày"),
                new CategorySeed("Phòng khách", "Đồ trang trí và đồ ngồi phòng khách"),
                new CategorySeed("Phòng ngủ", "Ga gối, đèn và đồ kê đầu giường"),
                new CategorySeed("Góc làm việc", "Đồ trên bàn làm việc"),
                new CategorySeed("Chăm sóc", "Đồ dùng phòng tắm và chăm sóc cá nhân"),
                new CategorySeed("Ngoài trời", "Đồ tưới cây và đồ mang ra ngoài"));
    }

    public static List<ProductSeed> products() {
        return List.of(
                new ProductSeed("Nhà bếp", "Ấm đun inox 1.7L", "Ấm đun nước đáy từ, vỏ inox, dung tích 1.7 lít.", 790_000),
                new ProductSeed("Nhà bếp", "Chảo chống dính 24cm", "Chảo cán gỗ, lòng chống dính, đường kính 24cm.", 459_000),
                new ProductSeed("Nhà bếp", "Bộ dao nhà bếp 5 món", "Năm lưỡi thép không gỉ và một giá cắm gỗ.", 890_000),
                new ProductSeed("Nhà bếp", "Hũ gốm đựng gia vị", "Bộ bốn hũ men mờ, nắp gỗ, để cạnh bếp.", 189_000),
                new ProductSeed("Nhà bếp", "Thớt gỗ cao su", "Thớt dày, mặt rộng, dùng thái rau và thịt nguội.", 259_000),
                new ProductSeed("Phòng khách", "Bình gốm men rạn", "Bình cắm cành, men rạn màu ngà.", 420_000),
                new ProductSeed("Phòng khách", "Đèn bàn vải lanh", "Chụp đèn lanh màu kem, chân gỗ sồi.", 650_000),
                new ProductSeed("Phòng khách", "Gối tựa linen", "Gối vuông vải lanh, ruột bông.", 290_000),
                new ProductSeed("Phòng khách", "Khay gỗ óc chó", "Khay trà hình chữ nhật, mặt gỗ óc chó.", 540_000),
                new ProductSeed("Phòng khách", "Nến thơm sả chanh", "Nến đậu nành, mùi sả chanh, cháy khoảng 40 giờ.", 159_000),
                new ProductSeed("Phòng ngủ", "Ga giường cotton 1m6", "Ga chun cotton mật độ cao, khổ 1m6.", 690_000),
                new ProductSeed("Phòng ngủ", "Đèn ngủ gốm", "Đèn ngủ ánh sáng ấm, thân gốm nhỏ.", 480_000),
                new ProductSeed("Phòng ngủ", "Gối nằm bông tự nhiên", "Gối nằm ruột bông, áo tháo giặt được.", 350_000),
                new ProductSeed("Phòng ngủ", "Chăn hè cotton", "Chăn mỏng cho đêm nóng, cotton mềm.", 520_000),
                new ProductSeed("Phòng ngủ", "Khay mây đầu giường", "Khay mây có quai, để sách và ly nước.", 310_000),
                new ProductSeed("Góc làm việc", "Giá sách để bàn", "Giá ba tầng, gỗ cao su, để sách và đồ nhỏ.", 430_000),
                new ProductSeed("Góc làm việc", "Đèn bàn LED", "Đèn kẹp bàn, ba mức sáng, ánh sáng trung tính.", 390_000),
                new ProductSeed("Góc làm việc", "Hộp bút gỗ sồi", "Hộp đựng bút nắp trượt, gỗ sồi.", 180_000),
                new ProductSeed("Góc làm việc", "Sổ tay giấy kem", "Sổ khâu chỉ, giấy kem không lem mực.", 95_000),
                new ProductSeed("Góc làm việc", "Kê tay bàn phím", "Đệm kê cổ tay vải canvas, ruột vụn cao su.", 220_000),
                new ProductSeed("Chăm sóc", "Khăn tắm cotton 60x120", "Khăn dệt thưa, thấm nước, khổ 60x120cm.", 210_000),
                new ProductSeed("Chăm sóc", "Bộ bình gốm phòng tắm", "Hai bình đựng dầu gội, men trắng.", 360_000),
                new ProductSeed("Chăm sóc", "Gương tròn khung mây", "Gương treo tường, đường kính 45cm.", 540_000),
                new ProductSeed("Chăm sóc", "Lược gỗ tự nhiên", "Lược răng thưa, gỗ tự nhiên, chống tĩnh điện.", 120_000),
                new ProductSeed("Chăm sóc", "Túi vải đựng đồ", "Túi canvas đáy đứng, dùng đi chợ hoặc đựng đồ giặt.", 89_000),
                new ProductSeed("Ngoài trời", "Bình tưới cây 1.5L", "Bình tưới vòi dài, dung tích 1.5 lít.", 175_000),
                new ProductSeed("Ngoài trời", "Chậu đất nung trung", "Chậu đất nung có lỗ thoát nước, đường kính 22cm.", 240_000),
                new ProductSeed("Ngoài trời", "Đèn năng lượng mặt trời", "Đèn cắm sân, tích điện ban ngày.", 320_000),
                new ProductSeed("Ngoài trời", "Thảm ngồi picnic", "Thảm cotton kẻ, gấp gọn, lót ngồi ngoài trời.", 450_000),
                new ProductSeed("Ngoài trời", "Giỏ mây đựng đồ", "Giỏ mây có quai, đựng trái cây hoặc đồ đi chơi.", 280_000));
    }
}

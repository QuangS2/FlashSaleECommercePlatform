# ==============================================================================
# Script Khởi tạo & Kiểm tra Kafka Topics cho Hệ thống Flash Sale (Windows PowerShell)
# ==============================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  KHỞI TẠO & KIỂM TRA PRODUCTION KAFKA TOPICS (3 PARTITIONS)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$topics = @(
    @{ Name = "order-events"; Retention = "604800000" },
    @{ Name = "inventory-events"; Retention = "604800000" },
    @{ Name = "payment-events"; Retention = "604800000" },
    @{ Name = "notification-events"; Retention = "86400000" }
)

foreach ($t in $topics) {
    $topicName = $t.Name
    $retention = $t.Retention
    Write-Host "`n--> Đang kiểm tra / tạo Topic: $topicName (Partitions: 3, Retention: $retention ms)..." -ForegroundColor Yellow
    docker exec ecommerce-kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic $topicName --partitions 3 --replication-factor 1 --config retention.ms=$retention
}

Write-Host "`n--> DANH SÁCH TOÀN BỘ TOPICS HIỆN CÓ:" -ForegroundColor Green
docker exec ecommerce-kafka kafka-topics --bootstrap-server localhost:9092 --list

Write-Host "`n--> CHI TIẾT CẤU HÌNH TOPIC order-events:" -ForegroundColor Green
docker exec ecommerce-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic order-events

Write-Host "`n[HOÀN TẤT] Cụm Kafka đã sẵn sàng cho Saga Choreography!`n" -ForegroundColor Cyan

export const inventoryService = {
  /**
   * Tra cứu thông tin tồn kho của sản phẩm từ Inventory Service (MySQL + Redis)
   */
  async fetchStock(productId: string): Promise<number | null> {
    try {
      const response = await fetch(`/api/v1/inventory/${productId}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      return typeof data.availableQuantity === 'number' ? data.availableQuantity : data.quantity || 0;
    } catch (error) {
      console.warn(`[InventoryService] Không thể tải tồn kho cho ${productId}:`, error);
      return null;
    }
  },

  /**
   * Kiểm tra nhanh sản phẩm còn đủ tồn kho không
   */
  async checkStock(productId: string, quantity: number = 1): Promise<boolean> {
    try {
      const response = await fetch(`/api/v1/inventory/${productId}/check?quantity=${quantity}`);
      if (!response.ok) {
        return false;
      }
      const isAvailable = await response.json();
      return Boolean(isAvailable);
    } catch {
      return true; // Graceful default
    }
  },

  /**
   * Khấu trừ tồn kho (Redisson Distributed Lock trên Backend)
   */
  async deductStock(productId: string, quantity: number = 1): Promise<boolean> {
    try {
      const response = await fetch(`/api/v1/inventory/deduct?productId=${productId}&quantity=${quantity}`, {
        method: 'POST',
      });
      return response.ok;
    } catch {
      return false;
    }
  },
};

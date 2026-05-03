package com.auction.client.service;

/**
 * Interface dùng để hứng kết quả trả về từ Server (thành công hoặc thất bại)
 * @param <T> Kiểu dữ liệu mong muốn trả về (Ví dụ: String, List<ItemDTO>, JsonObject...)
 */
public interface ApiCallback<T> {
  void onSuccess(T result);
  void onError(String errorMessage);
}

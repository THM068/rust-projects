use spin_sdk::http;
use spin_sdk::http::Response;

pub mod handlers;
pub mod models;
pub mod request_payloads;

pub fn render_as_json(content: &str, status: u16) -> Response {
    Response::builder()
        .status(status)
        .header("content-type", "application/json")
        .body(content.to_string())
        .build()
}
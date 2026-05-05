use spin_sdk::http::{Method, Request};
use spin_sdk::variables;
use crate::controllers::models::CreateLedger;

pub fn get_legder_request_payload(ledger_id: &str) -> Request {
    let blnk_base_url = variables::get("blnk_base_uri").unwrap_or_else(|_| "unknown".to_string());
    Request::builder()
        .method(spin_sdk::http::Method::Get)
        .uri(format!("{}/ledgers/{}", blnk_base_url, ledger_id))
        .header("content-type", "application/json")
        .build()
}

pub fn update_ledger_name_request_payload(ledger_id: &str, new_name: &str) -> Request {
    let blnk_base_url = variables::get("blnk_base_uri").unwrap_or_else(|_| "unknown".to_string());
    let payload = serde_json::json!({ "name": new_name }).to_string();
    Request::builder()
        .method(Method::Put)
        .uri(format!("{}/ledgers/{}", blnk_base_url, ledger_id))
        .header("content-type", "application/json")
        .body(payload)
        .build()
}
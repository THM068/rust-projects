use spin_sdk::http::Method;

pub fn get_method(method_name: &str) -> Method {
    match method_name {
        "GET" => Method::Get,
        "POST" => Method::Post,
        "PUT" => Method::Put,
        "DELETE" => Method::Delete,
        "PATCH" => Method::Patch,
        "HEAD" => Method::Head,
        "OPTIONS" => Method::Options,
        _ => Method::Get, // Default to GET for unknown methods
    }
}
use axum::response::IntoResponse;
use googletest::prelude::*;
use newsletter_app_web::controllers::greeting::Greeting;
use newsletter_app_web::controllers::health::Health;
use newsletter_app_web::test_helpers::{BodyExt, RouterExt, TestContext};
use newsletter_app_macros::test;

#[test]
async fn test_health_check(context: &TestContext) {
   let response = context.app.request("/health").send().await;
   
   assert!(response.status().is_success());
   let health: Health = response.into_body().into_json().await;
   assert_eq!(health.status, "OK");
   

    
}




use googletest::prelude::*;
use newsletter_app_macros::test;
use newsletter_app_web::controllers::greeting::Greeting;
use newsletter_app_web::test_helpers::{BodyExt, RouterExt, TestContext};

#[test]
async fn test_hello(context: &TestContext) {
    let response = context.app.request("/api").send().await;

    let greeting: Greeting = response.into_body().into_json().await;
    assert_that!(greeting.hello, eq(&String::from("world")));
}

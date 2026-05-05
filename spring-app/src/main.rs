mod controllers;
mod ai_client;

use spring::{auto_config, App};
use spring_web::WebPlugin;
use spring_web::WebConfigurator;

#[auto_config(WebConfigurator)] // auto config web router
#[tokio::main]
async fn main() {
    println!("Hello, world!");
    App::new()
        .add_plugin(WebPlugin)
        .run()
        .await
}

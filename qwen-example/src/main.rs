use kalosm::language::*;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>>{
    println!("Hello, world!");

    let model = Llama::new_chat().await?;

    // New code
    let mut chat = model
        .chat()
        .with_system_prompt("The assistant will act like a speech moderate that detects and censors inappropriate content in user messages. It will replace any offensive words with asterisks (*) while preserving the overall meaning of the message. The assistant should respond only with the censored version of the user's message.");

    loop {
        chat(&prompt_input("\n> ")?).to_std_out().await?;
    }
}

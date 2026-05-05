use rig::client::completion::CompletionClientDyn;
use rig::completion::CompletionModelDyn;
use rig::providers::deepseek::Client;

pub struct DeepSeekClient {
    pub client: Client
}

impl DeepSeekClient {
    pub fn new() -> Self {
        DeepSeekClient {
            client: Client::new("sk-c1ab2966c9bf4aa58b67b86a95012d21")
        }
    }
}
use reqwest::{Client, Error as ReqwestError};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::env;

#[derive(Debug, Clone)]
pub struct BlnkClient {
    client: Client,
    base_url: String,
    api_key: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Account {
    pub account_id: String,
    pub name: String,
    pub number: String,
    pub bank_name: String,
    pub currency: String,
    pub balance: f64,
    pub credit_balance: f64,
    pub debit_balance: f64,
    pub precision: u32,
    pub ledger_id: String,
    pub identity_id: String,
    pub indicator: String,
    pub created_at: String,
    pub updated_at: String,
    pub metadata: Option<HashMap<String, serde_json::Value>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CreateAccountRequest {
    pub name: String,
    pub number: String,
    pub bank_name: String,
    pub currency: String,
    pub ledger_id: String,
    pub identity_id: String,
    pub metadata: Option<HashMap<String, serde_json::Value>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CreateBalanceRequest {
    ledger_id: String,
    currency: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Balance {
    pub balance_id: String,
    pub balance: f64,
    pub credit_balance: f64,
    pub debit_balance: f64,
    pub currency: String,
    pub currency_multiplier: f64,
    pub ledger_id: String,
    pub identity_id: String,
    pub created_at: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub updated_at: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub metadata: Option<HashMap<String, serde_json::Value>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Transaction {
    pub transaction_id: String,
    pub source: String,
    pub destination: String,
    pub reference: String,
    pub amount: f64,
    pub currency: String,
    pub description: String,
    pub status: String,
    pub created_at: String,
    pub updated_at: String,
    pub metadata: Option<HashMap<String, serde_json::Value>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CreateTransactionRequest {
    pub source: String,
    pub destination: String,
    pub reference: String,
    pub amount: f64,
    pub currency: String,
    pub description: String,
    pub metadata: Option<HashMap<String, serde_json::Value>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Ledger {
    pub ledger_id: String,
    pub name: String,
    pub created_at: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub metadata: Option<HashMap<String, serde_json::Value>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CreateLedgerRequest {
    pub name: String,
    pub metadata: Option<HashMap<String, serde_json::Value>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ApiResponse<T> {
    pub data: T,
    pub message: String,
    pub status: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ApiError {
    pub error: String,
    pub message: String,
    pub status: String,
}

impl BlnkClient {
    /// Create a new Blnk client
    pub fn new(base_url: &str) -> Self {
        let client = Client::new();
        let api_key = env::var("BLNK_API_KEY").ok();

        Self {
            client,
            base_url: base_url.trim_end_matches('/').to_string(),
            api_key,
        }
    }

    /// Create a new Blnk client with API key
    pub fn with_api_key(base_url: &str, api_key: &str) -> Self {
        let client = Client::new();

        Self {
            client,
            base_url: base_url.trim_end_matches('/').to_string(),
            api_key: Some(api_key.to_string()),
        }
    }

    /// Helper method to build request with authentication
    fn build_request(&self, url: &str) -> reqwest::RequestBuilder {
        let mut request = self.client.get(url);

        if let Some(api_key) = &self.api_key {
            request = request.header("Authorization", format!("Bearer {}", api_key));
        }

        request.header("Content-Type", "application/json")
    }

    /// Helper method to build POST request with authentication
    fn build_post_request(&self, url: &str) -> reqwest::RequestBuilder {
        let mut request = self.client.post(url);

        if let Some(api_key) = &self.api_key {
            request = request.header("Authorization", format!("Bearer {}", api_key));
        }

        request.header("Content-Type", "application/json")
    }

    /// Helper method to build PUT request with authentication
    fn build_put_request(&self, url: &str) -> reqwest::RequestBuilder {
        let mut request = self.client.put(url);

        if let Some(api_key) = &self.api_key {
            request = request.header("Authorization", format!("Bearer {}", api_key));
        }

        request.header("Content-Type", "application/json")
    }

    /// Get server health/status
    pub async fn health(&self) -> Result<serde_json::Value, ReqwestError> {
        let url = format!("{}/health", self.base_url);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }

    /// Create a new ledger
    pub async fn create_ledger(&self, request: CreateLedgerRequest) -> Result<Ledger, ReqwestError> {
        let url = format!("{}/ledgers", self.base_url);
        let response = self.build_post_request(&url)
            .json(&request)
            .send()
            .await?;
        response.json().await
    }

    /// Get a ledger by ID
    pub async fn get_ledger(&self, ledger_id: &str) -> Result<Ledger, ReqwestError> {
        let url = format!("{}/ledgers/{}", self.base_url, ledger_id);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }

    /// List all ledgers
    pub async fn list_ledgers(&self) -> Result<Vec<Ledger>, ReqwestError> {
        let url = format!("{}/ledgers", self.base_url);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }

    /// Create a new account
    pub async fn create_account(&self, request: CreateAccountRequest) -> Result<ApiResponse<Account>, ReqwestError> {
        let url = format!("{}/accounts", self.base_url);
        let response = self.build_post_request(&url)
            .json(&request)
            .send()
            .await?;
        response.json().await
    }

    /// Get an account by ID
    pub async fn get_account(&self, account_id: &str) -> Result<ApiResponse<Account>, ReqwestError> {
        let url = format!("{}/accounts/{}", self.base_url, account_id);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }

    /// List all accounts
    pub async fn list_accounts(&self) -> Result<ApiResponse<Vec<Account>>, ReqwestError> {
        let url = format!("{}/accounts", self.base_url);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }
    // create_balance
    pub async fn create_balance(&self, request: CreateBalanceRequest) -> Result<Balance, ReqwestError> {
        let url = format!("{}/balances", self.base_url);
        let response = self.build_post_request(&url)
            .json(&request)
            .send()
            .await?;
        response.json().await
    }

    /// Get balance by ID
    pub async fn get_balance(&self, balance_id: &str) -> Result<Balance, ReqwestError> {
        let url = format!("{}/balances/{}", self.base_url, balance_id);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }

    /// List all balances
    pub async fn list_balances(&self) -> Result<ApiResponse<Vec<Balance>>, ReqwestError> {
        let url = format!("{}/balances", self.base_url);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }

    /// Create a new transaction
    pub async fn create_transaction(&self, request: CreateTransactionRequest) -> Result<ApiResponse<Transaction>, ReqwestError> {
        let url = format!("{}/transactions", self.base_url);
        let response = self.build_post_request(&url)
            .json(&request)
            .send()
            .await?;
        response.json().await
    }

    /// Get a transaction by ID
    pub async fn get_transaction(&self, transaction_id: &str) -> Result<ApiResponse<Transaction>, ReqwestError> {
        let url = format!("{}/transactions/{}", self.base_url, transaction_id);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }

    /// List all transactions
    pub async fn list_transactions(&self) -> Result<ApiResponse<Vec<Transaction>>, ReqwestError> {
        let url = format!("{}/transactions", self.base_url);
        let response = self.build_request(&url).send().await?;
        response.json().await
    }

    /// Update metadata for any resource
    pub async fn update_metadata(&self, resource_type: &str, resource_id: &str, metadata: HashMap<String, serde_json::Value>) -> Result<serde_json::Value, ReqwestError> {
        let url = format!("{}/{}/{}/metadata", self.base_url, resource_type, resource_id);
        let response = self.build_put_request(&url)
            .json(&metadata)
            .send()
            .await?;
        response.json().await
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tokio;

    #[tokio::test]
    async fn test_client_creation() {
        let client = BlnkClient::new("http://localhost:5001");
        assert_eq!(client.base_url, "http://localhost:5001");
    }

    #[tokio::test]
    async fn test_client_with_api_key() {
        let client = BlnkClient::with_api_key("http://localhost:5001", "test-key");
        assert_eq!(client.base_url, "http://localhost:5001");
        assert_eq!(client.api_key, Some("test-key".to_string()));
    }
}

// Example usage
#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Create client - use environment variable BLNK_API_KEY or pass it directly
    let client = BlnkClient::new("http://localhost:5001");

    // Or with API key
    // let client = BlnkClient::with_api_key("http://localhost:5001", "your-api-key");

    // Check health
    match client.health().await {
        Ok(health) => println!("Health check: {:?}", health),
        Err(e) => println!("Health check failed: {}", e),
    }

    // Create a ledger
    let ledger_request = CreateLedgerRequest {
        name: "Main Ledger".to_string(),
        metadata: Some({
            let mut map = HashMap::new();
            map.insert("environment".to_string(), serde_json::Value::String("development".to_string()));
            map
        }),
    };

    match client.create_ledger(ledger_request).await {
        Ok(ledger) => {
            println!("Created ledger: {:?}", ledger);

            // Create an account in the ledger
            let account_request = CreateAccountRequest {
                name: "User Account".to_string(),
                number: "ACC001".to_string(),
                bank_name: "Test Bank".to_string(),
                currency: "USD".to_string(),
                ledger_id: ledger.ledger_id.clone(),
                identity_id: "user123".to_string(),
                metadata: None,
            };

            match client.create_account(account_request).await {
                Ok(account) => {
                    println!("Created account: {:?}", account);

                    // Create a transaction
                    let transaction_request = CreateTransactionRequest {
                        source: "external".to_string(),
                        destination: account.data.account_id.clone(),
                        reference: "deposit001".to_string(),
                        amount: 100.0,
                        currency: "USD".to_string(),
                        description: "Initial deposit".to_string(),
                        metadata: None,
                    };

                    match client.create_transaction(transaction_request).await {
                        Ok(transaction) => println!("Created transaction: {:?}", transaction),
                        Err(e) => println!("Failed to create transaction: {}", e),
                    }
                }
                Err(e) => println!("Failed to create account: {}", e),
            }
        }
        Err(e) => println!("Failed to create ledger: {}", e),
    }

    Ok(())
}

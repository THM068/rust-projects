use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use anyhow::Context;
use uuid::Uuid;
use chrono::{DateTime, Utc};

#[derive(Debug, Deserialize, Serialize)]
pub struct CreateLedger {
   pub  name: String,
   pub  meta_data: HashMap<String, String>
}

impl TryFrom<&[u8]> for CreateLedger {
    type Error = anyhow::Error;

    fn try_from(value: &[u8]) -> Result<Self, Self::Error> {
        serde_json::from_slice::<CreateLedger>(value)
            .with_context(|| "Could not deserialize value into CreateAndUpdateItemModel")
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct Ledger {
    #[serde(rename = "ledger_id")]
    pub id: String,
    pub name: String,
    #[serde(rename = "created_at")]
    pub created_at: DateTime<Utc>,
    #[serde(rename = "meta_data")]
    pub metadata: HashMap<String, String>
}

#[derive(Debug, Clone, Serialize)]
pub struct ErrorResponse {
    pub error: String,
}

impl ErrorResponse {
    pub fn new(message: &str) -> Self {
        Self {
            error: message.to_string(),
        }
    }
}
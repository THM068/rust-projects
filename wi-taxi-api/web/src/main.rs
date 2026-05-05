#![allow(missing_docs)]
use std::process::ExitCode;
use wi_taxi_api_web::{init_tracing, run};

#[tokio::main]
async fn main() -> ExitCode {
    init_tracing();

    match run().await {
        Ok(_) => ExitCode::SUCCESS,
        Err(e) => {
            tracing::error!(
                error.msg = %e,
                error.error_chain = ?e,
                "Shutting down due to error"
            );
            ExitCode::FAILURE
        }
    }
}

use newsletter_app_config::Config;

use std::sync::Arc;
use newsletter_app_blnk_client::BlnkClient;

/// The application's state that is available in [`crate::controllers`] and [`crate::middlewares`].
pub struct AppState {
    pub blnk_client: BlnkClient
}

/// The application's state as it is shared across the application, e.g. in controllers and middlewares.
///
/// This is the [`AppState`] struct wrappend in an [`std::sync::Arc`].
pub type SharedAppState = Arc<AppState>;

/// Initializes the application state.
///
/// This function creates an [`AppState`] based on the current [`newsletter_app_config::Config`].
pub async fn init_app_state(config: Config) -> AppState {
    AppState {
        blnk_client: BlnkClient::new(config.blnk.url.as_str())
    } 
}

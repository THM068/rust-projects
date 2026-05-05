use spin_sdk::http::{IntoResponse, Request, Router};
use crate::handlers;

pub(crate) struct Api {
    router: Router,
}

impl Api {
    pub(crate) fn handle(&self, req: Request) -> anyhow::Result<impl IntoResponse> {
        Ok(self.router.handle(req))
    }
}

impl Default for Api {
    fn default() -> Self {
        let mut router = Router::default();
        

        router.post("/", handlers::convertMarkdownHandler);

        Api { router }
    }
}

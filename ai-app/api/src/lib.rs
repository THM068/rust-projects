use anyhow::{Context, Result};
use spin_sdk::{http, http::{Request, Response}, http_component, llm};
/// A simple Spin HTTP component.
#[http_component]
fn hello_world(req: Request) -> Result<Response> {
    let model = llm::InferencingModel::Llama2Chat;
    let inference = llm::infer(model, "Can you tell me a joke about cats");
    match inference {
        Err(e) => {
            println!("Inference error: {}", e);
            return Ok(http::Response::builder()
                .status(500)
                .body(format!("Inference error: {}", e)).build());
        },
        Ok(result) => {
            let text = result.text;
            return Ok(http::Response::builder()
                .status(200)
                .header("content-type", "text/html")
                .body(text.to_string()).build());
        }
    }
    // if let Ok(result) = &inference {
    //     let text = &result.text;
    //     return Ok(http::Response::builder()
    //         .status(200)
    //         .body(text.to_string()).build());
    // } else {
    //     return Ok(http::Response::builder()
    //         .status(500)
    //         .body("Inference failed".to_string()).build());
    // }
}

mod tests {
    #[test]
    fn test_vectors() {
        let array_vec = Vec::from([1,2,3,4,5]);
        assert_eq!(array_vec, vec![1,2,3,4,5]);
        
        let vec_string = Vec::from("hello");
        println!("{:?}", vec_string);
    }
}
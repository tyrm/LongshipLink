use worker::*;
use url::Url;
use serde_json::json;

#[event(fetch)]
async fn main(req: Request, env: Env, ctx: Context) -> Result<Response> {
    let url = Url::parse(&req.url().unwrap().to_string())?;
    let path = url.path();

    match path {
        "/api/v1/auth/user" => {
            let mut sid = None;
            let mut secret = None;
            let mut uid = None;

            for (key, value) in url.query_pairs() {
                match key.as_ref() {
                    "sid" => sid = Some(value.into_owned()),
                    "secret" => secret = Some(value.into_owned()),
                    "uid" => uid = Some(value.into_owned()),
                    _ => {}
                }
            }

            // Create a JSON object
            let params = json!({
                "sid": sid,
                "secret": secret,
                "uid": uid
            });

            // Convert the JSON object to a string
            let params_string = params.to_string();

            // Return the JSON string as the response
            Response::ok(params_string)
        },
        _ => Response::error("Not Found", 404),
    }
}

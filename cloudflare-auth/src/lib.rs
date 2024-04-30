use worker::*;
use url::Url;
use serde_json::json;

#[event(fetch)]
async fn main(req: Request, env: Env, _ctx: Context) -> Result<Response> {
    let router = Router::new();
    router
        .get_async("/api/v1/auth/user", authenticate_user)
        .run(req, env)
        .await
}

async fn authenticate_user(req: Request, ctx: RouteContext<()>) -> Result<Response> {
    let url = Url::parse(&req.url().unwrap().to_string())?;
    let kv = ctx.env.kv("ll-dev")?;

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

    // Return 400 error if any of the required parameters are missing
    if sid.is_none() || secret.is_none() || uid.is_none() {
        return Response::error("Missing required parameters", 400);
    }

    // read server secret by sidW
    let sid = sid.unwrap();
    let secret_key = format!("server.{}.secret", sid);
    let kv_secret = kv.get(&secret_key).text().await;
    console_debug!("kv_key: {}, kv_secret: {}", secret_key, kv_secret?.ok_or("secret not found").unwrap());

    // Create a JSON object
    let params = json!({
        "sid": sid,
        "secret": secret,
        "uid": uid,
    });
    Response::ok(params.to_string())
}

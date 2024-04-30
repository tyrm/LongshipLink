use worker::*;
use serde_json::json;
use serde::Deserialize;

#[event(fetch)]
async fn main(req: Request, env: Env, _ctx: Context) -> Result<Response> {
    let router = Router::new();
    router
        .get_async("/api/v1/auth/user", authenticate_user)
        .run(req, env)
        .await
}

#[derive(Deserialize)]
struct AuthQuery {
    sid: Option<String>,
    secret: Option<String>,
    uid: Option<String>,
}

async fn authenticate_user(req: Request, ctx: RouteContext<()>) -> Result<Response> {
    let kv = ctx.env.kv("ll-dev")?;

    let query: AuthQuery = req.query()?;

    // Return 400 error if any of the required parameters are missing
    if query.sid.is_none() || query.secret.is_none() || query.uid.is_none() {
        return Response::error("Missing required parameter", 400);
    }

    let sid = match query.sid {
        Some(sid) => sid,
        None => {
            return Response::error("Missing required parameter", 400);
        }
    };
    let secret = match query.secret {
        Some(secret) => secret,
        None => {
            return Response::error("Missing required parameter", 400);
        }
    };
    let uid = match query.uid {
        Some(uid) => uid,
        None => {
            return Response::error("Missing required parameter", 400);
        }
    };

    // read server secret by sid
    let secret_key = format!("server.{}.secret", sid);
    let kv_secret_result = kv.get(&secret_key).text().await;
    let kv_secret = match kv_secret_result {
        Ok(Some(secret)) => secret,
        Ok(None) => {
            console_debug!("Secret not found in KV: {}", secret_key);
            return Response::error("Unauthorized", 401);
        },
        Err(error) => {
            console_error!("Error reading secret from KV: {:?}", error);
            return Response::error("Internal Server Error", 500);
        }
    };
    console_debug!("kv_key: {}, kv_secret: {}", secret_key, kv_secret);

    // Compare the secret from the request with the secret from KV
    if secret != kv_secret {
        console_debug!("Secret mismatch: {} != {}", secret, kv_secret);
        return Response::error("Unauthorized", 401);
    };

    // Create a JSON object
    let params = json!({
        "sid": sid,
        "secret": secret,
        "uid": uid,
    });
    Response::ok(params.to_string())
}

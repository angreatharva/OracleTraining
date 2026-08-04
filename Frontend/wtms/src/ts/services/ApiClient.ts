import { appConfig, ServiceName, serviceUrl } from "../config/appConfig";

/** Error thrown for any non-2xx response. `body` holds the parsed error payload when present. */
export class ApiError extends Error {
  readonly status: number;
  readonly body: unknown;

  constructor(status: number, message: string, body: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
    // Required so `instanceof ApiError` works when compiled to ES5.
    Object.setPrototypeOf(this, ApiError.prototype);
  }
}

type Query = Record<string, string | number | boolean | null | undefined>;

interface RequestOptions {
  query?: Query;
  body?: unknown;
}

function withQuery(url: string, query?: Query): string {
  if (!query) {
    return url;
  }
  const params = new URLSearchParams();
  Object.keys(query).forEach((key) => {
    const value = query[key];
    if (value !== null && value !== undefined && value !== "") {
      params.append(key, String(value));
    }
  });
  const queryString = params.toString();
  return queryString ? `${url}?${queryString}` : url;
}

async function parseBody(response: Response): Promise<unknown> {
  if (response.status === 204) {
    return null;
  }
  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.indexOf("application/json") !== -1) {
    return response.json();
  }
  const text = await response.text();
  return text.length ? text : null;
}

/**
 * Thin `fetch` wrapper for the WealthTrack API Gateway.
 *
 * Deliberately does not retry: no backend endpoint is idempotent, so a retried
 * POST can debit money or apply a holding change twice.
 */
async function request<T>(
  method: string,
  service: ServiceName,
  path: string,
  options: RequestOptions = {}
): Promise<T> {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), appConfig.requestTimeoutMs);

  try {
    const response = await fetch(withQuery(serviceUrl(service, path), options.query), {
      method,
      headers:
        options.body === undefined
          ? { Accept: "application/json" }
          : { Accept: "application/json", "Content-Type": "application/json" },
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      signal: controller.signal
    });

    const body = await parseBody(response);

    if (!response.ok) {
      throw new ApiError(response.status, `${method} ${path} failed with ${response.status}`, body);
    }
    return body as T;
  } finally {
    window.clearTimeout(timeoutId);
  }
}

export const ApiClient = {
  get: <T>(service: ServiceName, path: string, query?: Query): Promise<T> =>
    request<T>("GET", service, path, { query }),

  post: <T>(service: ServiceName, path: string, body?: unknown): Promise<T> =>
    request<T>("POST", service, path, { body: body ?? {} }),

  put: <T>(service: ServiceName, path: string, body?: unknown): Promise<T> =>
    request<T>("PUT", service, path, { body: body ?? {} }),

  patch: <T>(service: ServiceName, path: string, body?: unknown): Promise<T> =>
    request<T>("PATCH", service, path, { body: body ?? {} }),

  del: (service: ServiceName, path: string): Promise<void> =>
    request<void>("DELETE", service, path)
};

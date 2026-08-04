/**
 * Application-wide configuration.
 *
 * All backend traffic goes through the WealthTrack API Gateway (default port 8081),
 * which strips the first path segment before forwarding. So `/trading/api/trade-transactions`
 * reaches Trading Service as `/api/trade-transactions`.
 *
 * NOTE: the gateway does not currently send CORS headers. Until it does, calls from the
 * `ojet serve` dev server origin will be blocked by the browser. Either add a CORS
 * configuration to the gateway or front it with a dev proxy.
 */

export const appConfig = {
  appName: "WealthTrack",

  /** Base URL of the API Gateway. */
  gatewayUrl: "http://localhost:8081",

  /** Gateway route prefixes, one per microservice. */
  servicePrefix: {
    user: "/user",
    bank: "/bank",
    portfolio: "/portfolio",
    trading: "/trading",
    product: "/product"
  },

  /** Milliseconds before an outbound request is aborted. */
  requestTimeoutMs: 15000
} as const;

export type ServiceName = keyof typeof appConfig.servicePrefix;

/** Builds a full gateway URL for a service-relative path such as `/api/users`. */
export function serviceUrl(service: ServiceName, path: string): string {
  return `${appConfig.gatewayUrl}${appConfig.servicePrefix[service]}${path}`;
}

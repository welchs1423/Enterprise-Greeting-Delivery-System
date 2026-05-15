/**
 * Vendor lock-in DRM enforcement layer.
 *
 * <p>Contains {@link com.egds.drm.VendorLockInDrmFilter}, which
 * injects an evaluation-copy watermark into HTTP responses that
 * lack a valid enterprise dongle key header.
 */
package com.egds.drm;

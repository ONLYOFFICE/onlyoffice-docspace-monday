/**
 * (c) Copyright Ascensio System SIA 2025
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onlyoffice.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation that can be applied to fields, parameters, and methods to validate that a String
 * contains a valid and safe URL.
 *
 * <p>This annotation performs several checks:
 *
 * <ul>
 *   <li>Validates the URL format
 *   <li>Verifies URL has proper hostname structure
 *   <li>Prevents unsafe protocols like JavaScript
 *   <li>Optionally enforces protocol requirement
 *   <li>Optionally enforces HTTPS-only
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>
 * public class WebsiteInfo {
 *     &#64;Url(requireProtocol = true, httpsOnly = true)
 *     private String websiteUrl;
 *
 *     // getters and setters
 * }
 * </pre>
 *
 * @see UrlValidator
 */
@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Url {
  /**
   * The message to be shown when validation fails.
   *
   * @return error message
   */
  String message() default "URL is invalid or potentially unsafe";

  /**
   * Allows attaching payload to a constraint to enrich validation information.
   *
   * @return the payload
   */
  Class<? extends Payload>[] payload() default {};

  /**
   * Whether to require the URL to have a protocol (http:// or https://).
   *
   * <p>When set to true, URLs without protocols will be rejected. When false, URLs without
   * protocols will be accepted and prefixed with "https://" during validation.
   *
   * @return true if protocol is required, false otherwise
   */
  boolean requireProtocol() default true;

  /**
   * Whether to limit the URL to only https protocol.
   *
   * <p>When set to true, only HTTPS URLs will be accepted. HTTP URLs will be rejected. This is
   * recommended for security-sensitive applications.
   *
   * @return true if only HTTPS is allowed, false if HTTP is also allowed
   */
  boolean httpsOnly() default true;

  /**
   * Specifies the validation groups to which this constraint belongs.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};
}

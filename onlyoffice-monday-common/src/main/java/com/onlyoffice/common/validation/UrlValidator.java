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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.regex.Pattern;

/**
 * Custom URL validator that implements the {@link ConstraintValidator} interface to validate URL
 * strings. This validator checks for properly formatted URLs and provides additional security
 * checks like JavaScript protocol rejection.
 *
 * <p>The validator can be configured to:
 *
 * <ul>
 *   <li>Require protocol specification (http:// or https://)
 *   <li>Enforce HTTPS-only URLs
 * </ul>
 */
public class UrlValidator implements ConstraintValidator<Url, String> {
  /** Pattern to detect JavaScript */
  private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("(?i)^\\s*javascript:");

  /** Pattern to validate proper hostname format */
  private static final Pattern HOSTNAME_PATTERN =
      Pattern.compile("^([\\w\\-]+\\.)+[\\w\\-]+(:\\d+)?(/.*)?$");

  /** Flag indicating whether the URL must include a protocol */
  private boolean requireProtocol;

  /** Flag indicating whether only HTTPS protocol is allowed */
  private boolean httpsOnly;

  /**
   * Initializes the validator with configuration from the {@link Url} annotation.
   *
   * @param constraintAnnotation the annotation instance for the validated element
   */
  public void initialize(Url constraintAnnotation) {
    this.requireProtocol = constraintAnnotation.requireProtocol();
    this.httpsOnly = constraintAnnotation.httpsOnly();
  }

  /**
   * Validates the given URL string according to the configured rules.
   *
   * <p>The validation includes:
   *
   * <ul>
   *   <li>Checking for null or empty values (considered valid)
   *   <li>Rejecting JavaScript protocol
   *   <li>Verifying protocol requirements (if configured)
   *   <li>Verifying HTTPS-only requirement (if configured)
   *   <li>Validating URL format and hostname pattern
   * </ul>
   *
   * @param value the URL string to validate
   * @param context context in which the constraint is evaluated
   * @return true if the URL is valid according to all rules, false otherwise
   */
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) return true;

    var normalizedUrl = value.trim();
    if (JAVASCRIPT_PATTERN.matcher(normalizedUrl).find()) return false;

    if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
      if (requireProtocol) return false;
      normalizedUrl = "https://" + normalizedUrl;
    } else if (httpsOnly && normalizedUrl.startsWith("http://")) {
      return false;
    }

    try {
      var url = URI.create(normalizedUrl).toURL();
      var host = url.getHost();
      return host != null && !host.isEmpty() && HOSTNAME_PATTERN.matcher(host).matches();
    } catch (MalformedURLException e) {
      return false;
    }
  }
}

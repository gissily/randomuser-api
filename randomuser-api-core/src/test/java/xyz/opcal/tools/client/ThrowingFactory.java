/*
 * Copyright 2020-2026 Opcal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.opcal.tools.client;

/**
 * Test-only {@link RandomUserClientFactory} whose {@code create(...)} always fails. When registered
 * first in a multi-implementation {@link java.util.ServiceLoader}, it lets a test prove the builder
 * uses the first discovered factory (the branch that logs a warning when more than one implementation
 * is found), because the failure surfaces from exactly that first factory.
 */
public class ThrowingFactory implements RandomUserClientFactory {

	@Override
	public RandomUserClient create(ClientConfig config) {
		throw new UnsupportedOperationException("throwing factory selected");
	}
}

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.client.transform;

import org.elasticsearch.client.ValidationException;
import org.elasticsearch.client.transform.transforms.TransformConfig;
import org.elasticsearch.client.transform.transforms.TransformConfigTests;
import org.elasticsearch.client.transform.transforms.latest.LatestConfigTests;
import org.elasticsearch.client.transform.transforms.pivot.PivotConfigTests;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.test.AbstractXContentTestCase;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.client.transform.transforms.SourceConfigTests.randomSourceConfig;
import static org.elasticsearch.test.hamcrest.OptionalMatchers.isEmpty;
import static org.elasticsearch.test.hamcrest.OptionalMatchers.isPresent;
import static org.hamcrest.Matchers.containsString;

public class PreviewTransformRequestTests extends AbstractXContentTestCase<PreviewTransformRequest> {
    @Override
    protected PreviewTransformRequest createTestInstance() {
        return new PreviewTransformRequest(TransformConfigTests.randomTransformConfig());
    }

    @Override
    protected PreviewTransformRequest doParseInstance(XContentParser parser) throws IOException {
        return new PreviewTransformRequest(TransformConfig.fromXContent(parser));
    }

    @Override
    protected boolean supportsUnknownFields() {
        return false;
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, Collections.emptyList());
        List<NamedXContentRegistry.Entry> namedXContents = searchModule.getNamedXContents();
        namedXContents.addAll(new TransformNamedXContentProvider().getNamedXContentParsers());

        return new NamedXContentRegistry(namedXContents);
    }

    public void testValidate() {
        assertThat(new PreviewTransformRequest(TransformConfigTests.randomTransformConfig()).validate(), isEmpty());
        assertThat(new PreviewTransformRequest(null).validate().get().getMessage(),
                containsString("preview requires a non-null transform config"));

        // null id and destination is valid
        TransformConfig config = TransformConfig.forPreview(randomSourceConfig(), PivotConfigTests.randomPivotConfig());

        assertThat(new PreviewTransformRequest(config).validate(), isEmpty());

        // null source is not valid
        config = TransformConfig.builder().setPivotConfig(PivotConfigTests.randomPivotConfig()).build();

        Optional<ValidationException> error = new PreviewTransformRequest(config).validate();
        assertThat(error, isPresent());
        assertThat(error.get().getMessage(), containsString("transform source cannot be null"));

        // null id and destination is valid
        config = TransformConfig.forPreview(randomSourceConfig(), LatestConfigTests.randomLatestConfig());

        assertThat(new PreviewTransformRequest(config).validate(), isEmpty());
    }
}

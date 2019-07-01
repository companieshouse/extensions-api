package uk.gov.companieshouse.extensions.api.reasons;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PatchReasonMapper {

    PatchReasonMapper INSTANCE = Mappers.getMapper(PatchReasonMapper.class);

    @Mappings({
        @Mapping(source = "reasonStatus", target = "reasonStatus")
    })
    ExtensionReasonEntity patchEntity(ExtensionCreateReason patchEntity,
                                      @MappingTarget ExtensionReasonEntity databaseEntity);
}

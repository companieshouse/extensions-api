package uk.gov.companieshouse.extensions.api.reasons;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PatchReasonMapper {

    PatchReasonMapper INSTANCE = Mappers.getMapper(PatchReasonMapper.class);

    @Mapping(target = "id", ignore = true)
    ExtensionReasonEntity mapPatchToEntity(ExtensionReasonEntity patchEntity,
                                           @MappingTarget ExtensionReasonEntity databaseEntity);
}

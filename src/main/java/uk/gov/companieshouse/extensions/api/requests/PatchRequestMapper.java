package uk.gov.companieshouse.extensions.api.requests;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PatchRequestMapper {

    PatchRequestMapper INSTANCE = Mappers.getMapper(PatchRequestMapper.class);

    ExtensionRequestFullEntity patchEntity(RequestStatus patchEntity,
                                          @MappingTarget ExtensionRequestFullEntity databaseEntity);
}